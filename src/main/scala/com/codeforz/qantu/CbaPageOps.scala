/*
 * Copyright 2012 CodeForz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codeforz.qantu

import com.codeforz.sclicks.WebPage
import java.util.Date
import grizzled.slf4j.Logging
import model.{AccountTransaction, AccountBalance}
import utils.CsvParser
import xml.XML
import scala.Some
import java.text.SimpleDateFormat
import java.io.File

/**
 * 
 */
class CbaPageOps extends PageOps with Logging{
  private val transSummary = ".homeMPMR table" //"#ctl00_BodyPlaceHolder_MyPortfolioGrid1_a"
  private val accountNameSelector = transSummary + " div[class='left'] a"
  private val accountTransLink = transSummary + " div[class='left'] a[title='%s']"
  private val acctListSelector = "#ctl00_ContentHeaderPlaceHolder_ddlAccount_field" //previous : ctl00_BodyPlaceHolder_blockAccount_ddlAccount_field"
  private val goButtonSelector =  "#ctl00_ContentHeaderPlaceHolder_lbGo" //previous : "input[value='GO']"

  private val advancedSearchTrigger =  "#cba_advanced_search_trigger"

  private val dateSearchRadio = "#ctl00_BodyPlaceHolder_radioSwitchDateRange_field_1" //#ctl00_BodyPlaceHolder_blockDates_rbtnChooseDates_field"
  private val startDateField = "#ctl00_BodyPlaceHolder_fromCalTxtBox_field"// "#ctl00$BodyPlaceHolder$fromCalTxtBox$field" //"#ctl00_BodyPlaceHolder_blockDates_caltbFrom_field"
  private val endDateField = "#ctl00_BodyPlaceHolder_toCalTxtBox_field"//#ctl00$BodyPlaceHolder$toCalTxtBox$field" //"#ctl00_BodyPlaceHolder_blockDates_caltbTo_field"
  private val searchButton = "#ctl00_BodyPlaceHolder_lbSearch" // "#ctl00_BodyPlaceHolder_blockAmount_btnSearch_field"

  private val transTable = "#transactionsTableBody"
  private val nextPageElement = ".paginator_control .page_next"
  private val pagingPanel = "#ctl00_BodyPlaceHolder_divPaging"

  private val logoffButton = "#ctl00_HeaderControl_ToolbarControl_logOffLink" // "#ctl00_HeaderControl_HeaderControlRV_ToolbarControl_logOffLink"
  private val loginField = "#txtMyClientNumber_field"
  private val passField = "#txtMyPassword_field"
  private val loginButton = "#btnLogon_field"
  private val homeTitle = "NetBank - Home"
  private val transPageTitle = "NetBank - Transactions"
  private val transPageAcctName = "#ctl00_BodyPlaceHolder_summaryAccountName" // "#ctl00_BodyPlaceHolder_gridViewAccount_r00_labelNickName_field"
  private val continueSearchLink = "a :content(continue searching)"
  private val formatter = new SimpleDateFormat("dd/MM/yyyy")

  val bankId = "CBA"
  val bankName = "Commonwealth Bank"
  val startUrl = "https://www.my.commbank.com.au/netbank/Logon/Logon.aspx"
  val maxTransHistoryDays = 730

  def doLogin(page: WebPage, userId: String, pass: String) {
    logger.debug("login page " + page.title)
    page.typeString(loginField, userId)
    page.typeString(passField, pass)
    page.waitForScripts(0)
    page.click(loginButton)
    page.waitForScripts(0)
    logger.debug("home page: " + page.title)
  }

  def getLoginError(page: WebPage) = {
    if(page.title.contains(homeTitle)) None
    else Some(page.find(".msg_cnt_wrp_error").map(_.text).getOrElse("Login Failure"))
  }

  def getAccountsBalance(page: WebPage) = {
    val summaryXml = page.asXml(transSummary)
    val rows = XML.loadString(summaryXml) \\ "table" \\ "tr"
    val headers = rows.head \\ "th" map (x => utils.cleanText(x.text))
    val values = rows.tail map {
      row => row \\ "td" map {
        td => utils.cleanText((td \ "div").filterNot(x => (x \ "@class" text) == "right").text) +
          utils.cleanText(td \ "span" text)
      }
    }
    logger.trace("Extracted values:\n %s\n%s".format(headers.mkString("\t"), values.map(_.mkString("\t")).mkString("\n")))
    //exclude footer summary
    values.filter(_.size == 5).map {
      fields => new AccountBalance(fields(0), fields(1), fields(2)
        , utils.parseDecimal(fields(3))
        , utils.parseDecimal(fields(4)))
    }
  }

  def getAccountNames(page: WebPage) = {
    page.all(accountNameSelector).map(_.attr("title")).filterNot(e=>e.contains("CommSec") || e.contains("MyWealth"))
  }

  def downloadTransactions(page:WebPage)= {
    if (page.find(".export").map(_.attr("class").contains("hidden")).getOrElse(true)) {
      warn("no transaction export link present.")
      page.saveTo(File.createTempFile("cba",".html").getAbsolutePath)
      Seq()
    } else{
      val dateFmt = new SimpleDateFormat("dd/MM/yyyy")
      page.click(".export a :content(Export)")
      page.first("#ctl00_ToobarFooterRight_ddlExportType_field").value = "CSV"
      page.downloadText("#ctl00_ToobarFooterRight_lbExport") map{text=>
        CsvParser.parseFile(text)
      } map {rows=>
        rows.map {values=>
          new AccountTransaction(dateFmt.parse(values(0)), utils.cleanText(values(2)), utils.parseDecimal(values(1)), values(2).startsWith("PENDING - "))
        }
      } getOrElse(Seq())
    }
  }

  def getTransactions(page:WebPage)= downloadTransactions(page)

  def isValidTransactionsPage(page: WebPage) = {
    if (!page.title.contains(transPageTitle)){
      error("Transaction page verification failed. Unexpected page title:" + page.title)
      false
    } else true
  }

  def openTransactionsPage(page: WebPage, account: String) {
    logger.info("loading trans page for " + account)
    page.waitForScripts(0)

    Iterator.from(1).takeWhile{i=>
      page.click(accountTransLink.format(account))
      if(!page.hasChanged && i<=2){
        warn("Retrying page loading...")
        true
      } else false
    }.size //force evaluation

    waitForPageLoad(page)
  }

  private def waitForPageLoad(page:WebPage)={
    page.waitForScripts(0)
    utils.waitUntil(page.find(".WhiteSpinner100").map(_.attr("class").contains("Loaded")).getOrElse{
      logger.warn("not found element matching .WhiteSpinner100")
      false}, 30)
  }

  def searchTransactions(page: WebPage, start: Date, end: Date) {
    logger.info("Searching trans from " + start + " to " + end)
    page.find(advancedSearchTrigger) match{
      case Some(e) =>
        e.click()
        //page.click(advancedSearchTrigger)
        page.click(dateSearchRadio)
        page.first(startDateField).value = formatter.format(start)
        page.first(endDateField).value = formatter.format(end)
        page.click(searchButton, 10000)
        waitForTransSearch(page)
        logger.debug("Search complete.")
      case _ => logger.warn("Search trigger element not found!")
    }
  }

  private def waitForTransSearch(page:WebPage){
    //logger.debug("trans-token elem:" + page.find(".divSearching").map(_.xml).getOrElse(""))
    page.waitForScripts(0)
    utils.waitUntil(page.find(".divSearching").map(_.attr("class").contains("Loaded")).getOrElse{
      logger.warn("not found element matching .divSearching")
      true}, 60)
  }

  def getSearchedTransactions(page: WebPage) = getTransactions(page)

  def hasTransSearchMorePages(page: WebPage) = {
    val hasMore = page.find(".trPager").map(!_.attr("class").contains("hidden")).getOrElse(false) &&
      page.find(pagingPanel).map(!_.attr("class").contains("hidden")).getOrElse(false) && page.find(continueSearchLink).isDefined
    logger.info("Has more pages:" + hasMore)
    hasMore
  }

  def transSearchNextPage(page: WebPage) {
    page.click(continueSearchLink, 20000)
    waitForTransSearch(page)
    logger.debug("next page loaded")
  }

  def openAccountsHome(page: WebPage) {
    page.waitForScripts(0)
    page.click("#MainMenu a :content(My home)")
    utils.waitUntil(page.find("#heading :content(My portfolio)").isDefined, 30)
  }

  def isValidAccountsHomePage(page: WebPage) = {
    page.find("#heading :content(My portfolio)").isDefined
  }
}
