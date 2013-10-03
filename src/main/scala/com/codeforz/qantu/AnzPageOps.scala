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

import grizzled.slf4j.Logging
import com.codeforz.sclicks.WebPage
import java.util.Date
import model.{AccountTransaction, AccountBalance}
import utils._
import xml.XML
import scala.Some
import java.text.SimpleDateFormat

/**
 * 
 */
class AnzPageOps extends PageOps with Logging{

  val bankId= "ANZ"
  val bankName = "ANZ Bank"
  val startUrl = "http://www.anz.com/personal/"
  val maxTransHistoryDays = 120

  def doLogin(page: WebPage, userId: String, pass: String) {
    page.click("#button_logon")
    page.typeString("frame[name='main'] input[name='CorporateSignonCorpId']",userId)
    page.typeString("frame[name='main'] input[name='CorporateSignonPassword']",pass)
    page.click("frame[name='main'] #SignonButton")
    page.waitForScripts(0)
  }

  def getLoginError(page: WebPage) = {
    if (page.find("#AcctsListOperative").isDefined) None
    else {
      Some("Login failed")
    }
  }

  def getAccountsBalance(page: WebPage) = {
    XML.loadString(page.asXml("#AcctsListOperative"))\\"tr" drop(2) dropRight(2) map{row =>
      val values = row\"td" map(_.text)
      val (bsb,acctNumb) = cleanText(values(1)).splitAt(6)
      new AccountBalance(cleanText(values(0)),bsb, acctNumb, parseDecimal(values(2)), parseDecimal(values(3)))
    }
  }

  def getAccountNames(page: WebPage) = page.all("#AcctsListOperative tr .dataCol a").map(e=>utils.cleanText(e.text))

  def getTransactions(page: WebPage) = {
    val df = new SimpleDateFormat("dd/MM/yyyy")
    page.find(".dtColTitleSelectDark :content(Processed)").flatMap(_.parent).flatMap(_.parent).map {e =>
      XML.loadString(e.xml)\\"tr" drop(2) map{row=>
        val cells = row \"td" map(_.text)
        new AccountTransaction(df.parse(cleanText(cells(0))), cleanText(cells(1)), parseDecimal(cleanText(cells(3))) - parseDecimal(cleanText(cells(2))))
      }
    }
  }.getOrElse{
    logger.warn("Transaction table not found!")
    Seq()
  }

  def isValidTransactionsPage(page: WebPage) = page.title.contains("ANZ Internet Banking")

  def openTransactionsPage(page: WebPage, account: String) {
    page.click("#AcctsListOperative tr .dataCol a :content("+account+")")
    page.waitForScripts(0)
  }

  def searchTransactions(page: WebPage, start: Date, end: Date) {
    val df = new SimpleDateFormat("dd/MM/yyyy")
    page.click(".transHistoryOptions a :content(Search)")
    page.waitForScripts(0)
    page.first("select[name='ANZSrchDtRng']").value = "7"
    page.typeString("input[name='ANZSrchFrmDt']",df.format(start))
    page.typeString("input[name='ANZSrchToDt']", df.format(end))
    page.click("input[value='search']")
    page.waitForScripts(0)
  }

  def getSearchedTransactions(page: WebPage) = getTransactions(page)

  def hasTransSearchMorePages(page: WebPage) = false

  def transSearchNextPage(page: WebPage) {} //no pagination supported

  def openAccountsHome(page: WebPage) {
    page.click("a :content(View Accounts)")
    page.waitForScripts(0)
  }

  def isValidAccountsHomePage(page: WebPage) = page.find("#AcctsListOperative").isDefined
}
