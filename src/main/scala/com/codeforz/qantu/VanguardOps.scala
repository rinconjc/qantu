package com.codeforz.qantu

import grizzled.slf4j.Logging
import com.codeforz.sclicks.WebPage
import java.util.Date
import scala.xml.XML
import com.codeforz.qantu.model.{AccountTransaction, AccountBalance}

/**
 *
 */
class VanguardOps extends PageOps with Logging{
  /**
   * Unique bank identifier, normally a abbreviated bank name
   */
  val bankId = "VANG"

  /**
   * The full name of the bank of financial institution
   */
  val bankName = "Vanguard Investment"

  /**
   * The bank URL where the navigation should start
   */
  val startUrl = "https://secure.vanguard.com.au/vanguard_online/"

  /**
   * The maximum number of days the transactions history is available in the website
   */
  val maxTransHistoryDays = 365

  /**
   * The login operation performed in the given `page` (the page loaded by the start url)
   */
  def doLogin(page: WebPage, userId: String, pass: String) {
    page.fill("frame[name='main'] #oform", "#username"->userId, "#password"->pass)(0)
    page.click("frame[name='main'] #submit")
    page.waitForScripts(0)
  }

  /**
   * Extract any login error from the given `page`, which resulted from the login operation
   */
  def getLoginError(page: WebPage) = page.find("frame[name='main'] .error").map(_.text)

  /**
   * Extract the accounts info and balance from the given accouns home `page`
   */
  def getAccountsBalance(page: WebPage) = page.all("frame[name='main'] #content tbody tr").drop(1).map {
    tr => val values = XML.loadString(tr.xml) \\ "td" map (_.text)
      val balance = utils.parseDecimal(values(3))
      new AccountBalance(tr.find("th").map(_.text).get, "","",balance, balance)
  }

  /**
   * Extract the account names from the given accouns home `page`
   */
  def getAccountNames(page: WebPage) = page.all("frame[name='main'] #content tbody tr").drop(1).flatMap(_.find("th").map(_.text))

  /**
   * Extracts the transactions from the given transactions `page`
   * @param page Some account transactions page
   */
  def getTransactions(page: WebPage) = page.all("frame[name='main'] #content tbody tr").map{
    tr => val row = XML.loadString(tr.xml) \\ "td" map (_.text)
      new AccountTransaction(utils.toDate(row(0), "dd MMM yyyy"), row(1), utils.parseDecimal(row(4)))
  }

  /**
   * Determines if the given `page` is a valid transaction page
   * @param page the possibly transaction page
   */
  def isValidTransactionsPage(page: WebPage) = page.find("frame[name='main'] h1 :content(Transaction History)").isDefined

  /**
   * Opens the transaction page for the given `account` from the accounts home `page`
   * @param page The accounts home page
   * @param account the account name to open the transactions page for
   */
  def openTransactionsPage(page: WebPage, account: String) {
    page.click(s"frame[name='main'] #content tbody th a :content($account)")
    page.waitForScripts(0)
  }

  /**
   * Executes a search in the given transaction `page`
   * @param page some account's transaction page
   * @param start the transactions start date
   * @param end the transactions end date
   */
  def searchTransactions(page: WebPage, start: Date, end: Date) {
    warn("search not supported")
  }

  /**
   * Extracts transactions from the given transactions `page`
   * @param page some account's transaction page
   */
  def getSearchedTransactions(page: WebPage) = getTransactions(page)

  /**
   * Determines if there are more transaction pages
   * @param page the transaction search results page
   */
  def hasTransSearchMorePages(page: WebPage) = false

  /**
   * Opens the transactions next page
   * @param page the current transactions page
   */
  def transSearchNextPage(page: WebPage) {
    warn("not supported")
  }

  /**
   * Opens the accounts home page from the given transaction `page`
   * @param page the transactions page
   */
  def openAccountsHome(page: WebPage) {
    page.click("frame[name='main'] #LHSnav a :content(Investment Summary)")
    page.waitForScripts(0)
  }

  /**
   * Determines if the given `page` is a valid account home page
   * @param page the account home page
   */
  def isValidAccountsHomePage(page: WebPage) = page.find("frame[name='main'] h1 :content(Investment Summary)").isDefined
}
