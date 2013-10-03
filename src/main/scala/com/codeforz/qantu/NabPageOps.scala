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
import java.io.File
import grizzled.slf4j.Logging
import model.{AccountTransaction, AccountBalance}
import xml.XML
import utils._
import java.text.SimpleDateFormat

/**
 * 
 */
class NabPageOps extends PageOps with Logging{
  private val bsbAcctNo = """BSB:\s*([^\s]+)\s*Acct No:\s*([^\s]+)\s*""".r

  val bankId = "NAB"
  val bankName = "National Australia Bank"
  val startUrl = "http://www.nab.com.au/" //"https://ib.nab.com.au/nabib/index.jsp" // "http://www.nab.com.au/"
  val maxTransHistoryDays = 560

  def doLogin(page: WebPage, userId: String, pass: String) {
    page.click("input[value='Login']")
    if(page.find("iframe[name='ibPreloginMessage']").isDefined){
      log.info("ignoring pre-login message")
      page.click("#optOut")
      page.click("a[href*='goToLogin']")
    }

    utils.waitUntil(page.title.contains("NAB Internet Banking"), 15)

    page.typeString("input[name='userid']", userId)
    page.typeString("input[name='password']", pass)
    page.click("a :content(Login)", 30000)
  }

  def getLoginError(page: WebPage) = {
    if (page.find("a :content(Logout)").isDefined) None
    else{
      val tmpfile = File.createTempFile("nab", ".html").getAbsolutePath
      error("Login test failed. Dumping page to:" + tmpfile)
      Some(page.find("#errorMessage").map(_.text).getOrElse("Login failure"))
    }
  }

  def getAccountsBalance(page: WebPage) = {
    XML.loadString(page.asXml("#accountBalances_nonprimary_subaccounts"))\\"tr" drop(1) map{row=>
      val cells = row\"td"
      val acctName = (cells.head \\ "span").find(e => (e \ "@class").text.contains("accountNickname")).map(_.text.trim).head
      val bsbAcctNo(bsb,number) = (cells.head \\"div").find(e=>(e\"@class").text.contains("accountNumber")).head.text.trim

      new AccountBalance(acctName, bsb, number, cells(1).text, cells(2).text)
    }
  }

  def getAccountNames(page: WebPage) = {
    page.all("span[class='accountNickname']").map(_.text.trim)
  }

  def getTransactions(page: WebPage) = {
    val rows = XML.loadString(page.asXml("#transactionHistoryTable")) \\ "tr" drop (1)
    rows.map(row=>row \ "td" map (_.text.trim)).filter(_.size >= 3).map {
      cells =>
        val debit: BigDecimal = cells(2)
        val credit: BigDecimal = cells(3)
        new AccountTransaction(toDate(cells(0), "dd MMM yy"), cleanText(cells(1)), credit + debit, false)
    }
  }

  def isValidTransactionsPage(page: WebPage) = {
    if(page.title.contains("Transaction history")) true
    else{
      error("Failed to open transactions page. Page title: " + page.title)
      false
    }
  }

  def openTransactionsPage(page: WebPage, account: String) {
    page.find("a[class='accountNickname'] :content(" + account + ")") match{
      case Some(e) =>
        e.parent.foreach(_.find("a :content(Transactions)").foreach(_.click()))
        page.click("#showFilterLink")
      case _ => error("Transaction link not found for account " + account)
    }
  }

  def searchTransactions(page: WebPage, start: Date, end: Date) {
    val sdf = new SimpleDateFormat("dd/MM/yy")
    log.info("searching trans between " + start + " and " + end)
    page.typeString("#periodFromDateD", sdf.format(start))
    page.typeString("#periodToDateD", sdf.format(end))
    page.click("#displayButton")
  }

  def getSearchedTransactions(page: WebPage) = getTransactions(page)

  def hasTransSearchMorePages(page: WebPage) = page.find("a :content(Next)").isDefined

  def transSearchNextPage(page: WebPage) {
    page.click("a :content(Next)")
  }

  def openAccountsHome(page: WebPage) {
    page.click("a :content(Account summary)")
  }

  def isValidAccountsHomePage(page: WebPage) = {
    if (page.find("#accountBalances_nonprimary_subaccounts").isDefined) true
    else{
      error("Invalid accounts page:" + page.title)
      false
    }
  }
}
