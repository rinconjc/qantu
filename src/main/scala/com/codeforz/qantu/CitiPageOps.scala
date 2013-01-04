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
import model.AccountBalance
import utils._
import xml.XML
import scala.Some

/**
 * 
 */
object CitiPageOps extends PageOps with Logging{

  val bankId = "CITI"
  val bankName = "Citibank Australia"
  val startUrl = "https://www.citibank.com.au/AUGCB/JSO/signon/DisplayUsernameSignon.do"
  val maxTransHistoryDays = 0

  def doLogin(page: WebPage, userId: String, pass: String) {
    page.typeString("#username",userId)
    page.click("#password")
    page.click("#largevkb .wbuttonlarge :content(Clear)")
    pass.toUpperCase.foreach(c=>page.click("#largevkb .wbuttonlarge :content(" + c + ")"))
    page.click("#largevkb a :content(OK)")
    page.waitForScripts(0)
    page.click("td input[src*='ck_btn.gif']")
    page.waitForScripts(0)
  }

  def getLoginError(page: WebPage) = if (page.find("#hostaccountSummary").isDefined) None
  else{
    error("Citi login failure:" + page.title)
    Some(page.find(".apptxtlg").map(_.text).getOrElse("Login failure"))
  }

  def getAccountsBalance(page: WebPage) = {
    val tbl = XML.loadString(page.first("#hostaccountSummary .table1").xml)
    tbl\\"tr" map(_\\"td") filter(_.size ==7) map {cells=>
      val values = cells map(_.text)
      new AccountBalance(cleanText(values(2)),"",cleanText(values(3)), values(4).replaceAll("[^\\d,.-]",""), values(5).replaceAll("[^\\d,.-]",""))
    }
  }

  def getAccountNames(page: WebPage) = {
    val tbl = XML.loadString(page.first("#hostaccountSummary .table1").xml)
    tbl\\"tr" map(_\\"td") filter(_.size ==7) map(_(2).text.trim)
  }

  def getTransactions(page: WebPage) = Seq() //trans not supported yet

  def isValidTransactionsPage(page: WebPage) = true

  def openTransactionsPage(acctsHome: WebPage, account: String) {
    //trans not supported yet
  }

  def searchTransactions(page: WebPage, start: Date, end: Date) {
    //trans not supported yet
  }

  def getSearchedTransactions(page: WebPage) = getTransactions(page)

  def hasTransSearchMorePages(page: WebPage) = false

  def transSearchNextPage(page: WebPage) {}

  def openAccountsHome(page: WebPage) {}

  def isValidAccountsHomePage(page: WebPage) = page.find("#hostaccountSummary").isDefined
}
