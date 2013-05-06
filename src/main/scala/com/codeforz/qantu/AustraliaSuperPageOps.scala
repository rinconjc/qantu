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

/**
 * 
 */
object AustraliaSuperPageOps extends PageOps{

  val bankId = "AUSUP"
  val bankName = "Australia Super"
  val startUrl = "https://www.australiansuper.com/login.aspx"

  override val enabled = false

  def maxTransHistoryDays = 36500

  def doLogin(page: WebPage, userId: String, pass: String) {
    page.typeString("#UsernameText", userId)
    page.typeString("#PasswordText", pass)
    page.click("#btnSubmit")
  }

  def getLoginError(page: WebPage) = {
    page.find(".fn_inlineValidation_errors").map(_.text)
  }

  def getAccountsBalance(page: WebPage) = {

    null
  }

  def getAccountNames(page: WebPage) = null

  def getTransactions(page: WebPage) = null

  def isValidTransactionsPage(page: WebPage) = false

  def openTransactionsPage(acctsHome: WebPage, account: String) {}

  def searchTransactions(page: WebPage, start: Date, end: Date) {}

  def getSearchedTransactions(page: WebPage) = null

  def hasTransSearchMorePages(page: WebPage) = false

  def transSearchNextPage(page: WebPage) {}

  def openAccountsHome(page: WebPage) {}

  def isValidAccountsHomePage(page: WebPage) = false
}
