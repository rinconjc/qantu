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

import model.{AccountBalance, AccountTransaction}
import java.util.{ServiceLoader, Calendar, Date}
import com.codeforz.sclicks.WebPage
import utils.{PageWrapper, PageRef}
import grizzled.slf4j.Logging
import com.gargoylesoftware.htmlunit.BrowserVersion
import scala.collection.JavaConverters._
/**
* Bank accounts data extractor API entry point.
 *
*/
object AccountExtractor{

  //loads all PageOps implementations from the classpath, and registers dynamically.
/*
  private val extractors = new Reflections("com.codeforz.qantu").getSubTypesOf(classOf[PageOps]).map{c =>
    c.getField("MODULE$").get(classOf[PageOps]).asInstanceOf[PageOps]}.filter(_.enabled).map(p=>(p.bankId->p)).toMap
*/
  private val extractors = ServiceLoader.load(classOf[PageOps]).asScala.map(po=> po.bankId -> po).toMap

  def supportedBanks = extractors.map{case (id, ops) => (id, ops.bankName)}

  def get(bankId:String) = extractors.get(bankId) map (new AccountExtractor()(_))

  def apply(bankId:String) = new AccountExtractor()(extractors.getOrElse(bankId, sys.error("Data extractor not found for bank id:" + bankId)))
}

/**
 * Main class for account data extraction lifecycle
 */
class AccountExtractor(implicit ops:PageOps) {
  private val navigator = new AccountPageNavigator()(ops)

  /**
  * login using the given credential
  */
  def login(userId:String, pass:String) = navigator.login(userId, pass)

  /**
  * log out and terminate session
  */
  def logout(){
    navigator.logoff()
  }

  /**
  * Retrieve all accounts details including balance amounts
  */
  def allAccountsBalance:Seq[AccountBalance] = navigator.home.accountsBalance

  /**
  * Retrieves transactions for the `account` in the specified date range
  */
  def retrieveTransactions(account:String, start:Date, end:Date):Iterator[AccountTransaction]={
    val transPage = navigator.transPage(account).search(start, end)
    (Iterator(transPage.searchedTransactions) ++ new Iterator[Seq[AccountTransaction]]{
      def hasNext = transPage.hasMorePages
      def next() = transPage.nextPage().searchedTransactions
    }).flatten
  }

  /**
  * Retrieves all transaction history for the given `account`
  */
  def retrieveAllTransactions(account:String):Iterator[AccountTransaction]={
    val c = Calendar.getInstance()
    c.add(Calendar.DATE, - ops.maxTransHistoryDays)
    retrieveTransactions(account, c.getTime, new Date())
  }

  /**
  * Retrieve recent transactions for the given `account`
  *
  */
  def retrieveRecentTransactions(account:String):Iterator[AccountTransaction]={
    navigator.transPage(account).transactions.iterator
  }

  /**
  * Extract account names
  */
  def accountNames:Seq[String] = navigator.home.accountNames

}


/**
* Class that drives navigation across different pages: Accounts Home, Transactions Page, etc.
*/
class AccountPageNavigator(implicit ops:PageOps) extends Logging{
  //holds a reference to current page
  private val pageRef = new PageRef[PageWrapper](StartPage(ops.startUrl))

  /**
  * Tries to login to the site with the given credentials
  */
  def login(userId:String, pass:String)={
    pageRef.update{p=>
      try{
        ops.doLogin(p, userId, pass)
        AccountsHome(p)
      }catch{
        case t:Exception =>
          error("Login failure", t)
          throw new LoginFailureException(t.getMessage)
      }
    }
  }

  /**
  * Navigates to accounts home page
  */
  def home = pageRef.get match{
    case StartPage(_) => sys.error("Not logged in")
    case p:AccountsHome => p
    case _ =>
      pageRef.update{p=>
        ops.openAccountsHome(p)
        AccountsHome(p)
      }
  }

  /**
  * Navigates to the transactions page of the specified `account`
  */
  def transPage(acct:String) = {
    info("opening trans page for " + acct)
    home
    pageRef.update{p=>
      ops.openTransactionsPage(p, acct)
      TransactionPage(p)
    }
  }

  /**
  * Searches the transactions for the given `account`
  */
  def searchTrans(acct:String, start:Date, end:Date)={
    val c = Calendar.getInstance()
    c.add(Calendar.DATE, - ops.maxTransHistoryDays)
    val actualStart = if (start.before(c.getTime)) c.getTime else start
    info("Searching transactions for " + acct + " from " + actualStart + " to " + end)
    transPage(acct)
    pageRef.update{p=>
      ops.searchTransactions(p, actualStart, end)
      TransactionPage(p)
    }
  }

  /**
  * Logs off and terminates the current session
  */
  def logoff(){
    info("logging off")
   pageRef.update{p=>
     p.closeAll()
     StartPage(ops.startUrl)
   }
  }

}

/**
*  The starting page wrapper. It normally opens the site URL and start the navigation session.
*/
case class StartPage(url:String)(implicit ops:PageOps) extends PageWrapper{
  def page = WebPage.open(url)(ops.browserVersion)
  def isValid = true
}

/**
* Accounts home page
*/
case class AccountsHome(page:WebPage)(implicit ops:PageOps) extends PageWrapper {

  def accountsBalance:Seq[AccountBalance] = ops.getAccountsBalance(page)

  def accountNames:Seq[String] = ops.getAccountNames(page)

  def isValid = ops.isValidAccountsHomePage(page)

  override def getError = ops.getLoginError(page)
}

/**
* Transactions page
*/
case class TransactionPage(page:WebPage)(implicit ops:PageOps) extends PageWrapper{
  def isValid = ops.isValidTransactionsPage(page)

  def transactions = ops.getTransactions(page)
  def search(start:Date,end:Date)={
    ops.searchTransactions(page, start, end)
    this
  }

  def searchedTransactions = ops.getSearchedTransactions(page)

  def hasMorePages = ops.hasTransSearchMorePages(page)
  def nextPage() = {
    ops.transSearchNextPage(page)
    this
  }
}

/**
* Web page operations.
* Interface to be implemented for each financial institution website.
*/
trait PageOps{
  /**
  * Unique bank identifier, normally a abbreviated bank name
  */
  def bankId:String

  /**
  * The full name of the bank of financial institution
  */
  def bankName:String

  /**
  * whether this implementation is production ready
  */
  def enabled = true

  /**
  * The bank URL where the navigation should start
  */
  def startUrl:String

  /**
  * The specific browser version to use when navigating this website
  */
  def browserVersion:BrowserVersion = BrowserVersion.FIREFOX_17

  /**
  * The maximum number of days the transactions history is available in the website
  */
  def maxTransHistoryDays:Int

  /**
  * The login operation performed in the given `page` (the page loaded by the start url)
  */
  def doLogin(page:WebPage, userId:String, pass:String)

  /**
  * Extract any login error from the given `page`, which resulted from the login operation
  */
  def getLoginError(page:WebPage):Option[String]

  /**
  * Extract the accounts info and balance from the given accouns home `page`
  */
  def getAccountsBalance(page:WebPage):Seq[AccountBalance]

  /**
  * Extract the account names from the given accouns home `page`
  */
  def getAccountNames(page:WebPage):Seq[String]

  /**
  * Extracts the transactions from the given transactions `page`
   * @param page Some account transactions page
  */
  def getTransactions(page:WebPage):Seq[AccountTransaction]

  /**
  * Determines if the given `page` is a valid transaction page
  * @param page the possibly transaction page
  */
  def isValidTransactionsPage(page:WebPage):Boolean

  /**
  * Opens the transaction page for the given `account` from the accounts home `page`
  * @param page The accounts home page
  * @param account the account name to open the transactions page for
  */
  def openTransactionsPage(page:WebPage, account:String)

  /**
  * Executes a search in the given transaction `page`
    * @param page some account's transaction page
    * @param start the transactions start date
    * @param end the transactions end date
  */
  def searchTransactions(page:WebPage, start:Date, end:Date)

  /**
  * Extracts transactions from the given transactions `page`
  * @param page some account's transaction page
  */
  def getSearchedTransactions(page:WebPage):Seq[AccountTransaction]

  /**
  * Determines if there are more transaction pages
  * @param page the transaction search results page
  */
  def hasTransSearchMorePages(page:WebPage):Boolean

  /**
  * Opens the transactions next page
   * @param page the current transactions page
  */
  def transSearchNextPage(page:WebPage)

  /**
  * Opens the accounts home page from the given transaction `page`
  * @param page the transactions page
  */
  def openAccountsHome(page:WebPage)

  /**
  * Determines if the given `page` is a valid account home page
  * @param page the account home page
  */
  def isValidAccountsHomePage(page:WebPage):Boolean
}
