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

/**
 * 
 */
class WestpacPageOps extends PageOps with Logging{

  val bankId = "WESTPAC"
  val bankName = "Westpac Bank"
  override val enabled = false
  val startUrl = "http://www.westpac.com.au/"
  val maxTransHistoryDays = 120

  def doLogin(page: WebPage, userId: String, pass: String) {
    page.click("a :content(Westpac Online Banking)")
    page.typeString("#username_temp", userId)
    val keyToButton = page.all(".keypad .keys button").map(e=>(e.text, e)).toMap
    pass.toUpperCase.foreach(c=>keyToButton(c.toString).click())
    page.click("#pwd_submit")
    page.waitForScripts(0,30) //wait for any running js for up to 30 secs
  }

  def getLoginError(page: WebPage) = {
    if (page.title.contains("Sign in Error"))
      Some("Login failure")
    else
      None
  }

  def getAccountsBalance(page: WebPage) = {
    //TODO: parse accounts balance from the accounts page
    null
  }

  def getAccountNames(page: WebPage) = {
    //TODO: parse acocunt names from the accounts page
    null
  }

  def getTransactions(page: WebPage) = {
    //TODO: parse/extract transactions from the transactions page
    null
  }

  def isValidTransactionsPage(page: WebPage) = {
    //TODO: validate transactions page
    false
  }

  def openTransactionsPage(acctsHome: WebPage, account: String) {
    //TODO: open transactions page from the accounts page for the given account
  }

  def searchTransactions(page: WebPage, start: Date, end: Date) {
    //TODO: search transactions in the current transactions page
  }

  def getSearchedTransactions(page: WebPage) = {
    //TODO: parse/extract transactions from the current search result page
    null
  }

  def hasTransSearchMorePages(page: WebPage) = {
    //TODO: check if there's any more pages in the current search results page
    false
  }

  def transSearchNextPage(page: WebPage) {
    //TODO: move to the next page
  }

  def openAccountsHome(page: WebPage) {
    //TODO: move to the accounts page from the current transactions page
  }

  def isValidAccountsHomePage(page: WebPage) = {
    //TODO: check if page is a valid accounts page
    false
  }
}
