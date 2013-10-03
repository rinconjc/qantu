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
import java.util.{Properties, Date}
import com.gargoylesoftware.htmlunit.BrowserVersion
import java.io.{InputStream, File}
import java.security.MessageDigest
import grizzled.slf4j.Logging
import collection.JavaConversions._
import model.{AccountTransaction, AccountBalance}
import xml.XML
import scala.Some
import utils._
import java.text.SimpleDateFormat

/**
 * 
 */
class MyRateIngPageOps extends PageOps with Logging{
  private lazy val digitsHash = {
    val props = new Properties()
    props.load(getClass.getResourceAsStream("ing/digits-md5.properties"))
    logger.info("digits hash loaded:" + props)
    props.toMap
  }

  val bankId = "MYRATE"
  val bankName = "MyRate(ING)"
  val startUrl = "https://www.ingnv.com.au/client/Index.aspx"
  val maxTransHistoryDays = 1825

  //override def browserVersion = BrowserVersion.FIREFOX_3_6

  def doLogin(page: WebPage, userId: String, pass: String) {
    page.typeString("frame[name='body'] #txtCIF", userId)
    val images = page.all("frame[name='body'] #objKeypad_divShowAll input[type='image']")
    val digitsToImg = images.map(img => (digitFrom(page.getAsStream(img.attr("src"))), img)).toMap

    pass.map(c => digitsToImg(Some(c + ""))).foreach(_.click())
    page.click("frame[name='body'] #btnLogin")
    /*page.waitForScripts(1,30)//comment after update
    page.find("frame[name='leftmenu']").filter(_.attr("src")!="Menu.aspx").foreach(_.attr("src","Menu.aspx"))//comment after update*/
    page.waitForScripts(1,30)
    page.click("frame[name='leftmenu'] a[href*='myfinances']")
  }

  private def digitFrom(is: InputStream) = {
    val digest = MessageDigest.getInstance("MD5")
    collectBytes(is) {
      digest.update(_, 0, _)
    }
    val hash = digest.digest().map("%1$02X".format(_)).mkString
    logger.info("md5 hash:" + hash)
    digitsHash.get(hash)
  }

  private def collectBytes(is: InputStream)(consumer: (Array[Byte], Int) => Unit) {
    val bytes = Array.ofDim[Byte](1024)
    def readChunk() {
      val len = is.read(bytes)
      if (len >= 0) {
        consumer(bytes, len)
        readChunk()
      }
    }
    readChunk()
    is.close()
  }

  def getLoginError(page: WebPage) = {
    //if (!page.title.contains("Finances")){
    if (page.find("frame[name='body'] #dgAccountList").isEmpty){
      val tmpFile = File.createTempFile("ing", ".html")
      page.saveTo(tmpFile.getAbsolutePath)
      Some(page.title)
    } else None
  }

  def getAccountsBalance(page: WebPage) = {
    val xml = XML.loadString(page.first("frame[name='body'] #dgAccountList").xml)
    xml \\ "tr" drop (1) map {
      row =>
        val fields = row \\ "td" drop (1) map (e=>removeBlanks(e.text))
        new AccountBalance(fields(0), "", fields(1), parseDecimal(fields(3)), parseDecimal(fields(2)))
    }
  }

  def getAccountNames(page: WebPage) = {
    val xml = XML.loadString(page.first("frame[name='body'] #dgAccountList").xml)
    xml\\"tr" drop(1) map{
      row => removeBlanks((row \\"td")(1).text)
    }
  }

  def getTransactions(page: WebPage) = {
    page.click("frame[name='body'] #btn_Continue")
    getSearchedTransactions(page)
  }

  def isValidTransactionsPage(page: WebPage) = {
    page.title == "MoreTransactions"
  }

  def openTransactionsPage(page: WebPage, account: String) {
    page.find("frame[name='body'] #dgAccountList td :content(" + account + ")") match{
      case Some(td) => td.parent.foreach(_.find("input[type='image']").foreach(_.click()))
      case _ => error("Transaction link not found for account " + account)
    }
    page.click("frame[name='body'] #btnMore")
  }

  def searchTransactions(page: WebPage, start: Date, end: Date) {
    val dateFormat = new SimpleDateFormat("dd/MM/yyyy")
    page.click("frame[name='body'] #optList_Duration3")
    page.first("frame[name='body'] #TextBoxFrom").value = dateFormat.format(start)
    page.first("frame[name='body'] #TextBoxTo").value = dateFormat.format(end)
    page.click("frame[name='body'] #btn_Continue")
  }

  def getSearchedTransactions(page: WebPage) = {
    page.find("frame[name='body'] table #dgMoreResults").map{e=>
      XML.loadString(e.xml) \\ "tr" drop (1) map {
        row =>
          val values = row \ "td" map (e => removeBlanks(e.text))
          val fields = Seq(values(0), values(2), values(3), values(4))
          val debit: BigDecimal = if (fields(2).isEmpty) BigDecimal(0) else parseDecimal(fields(2))
          val credit: BigDecimal = if (fields(3).isEmpty) BigDecimal(0) else parseDecimal(fields(3))
          new AccountTransaction(new SimpleDateFormat("dd/MM/yyyy").parse(fields(0)), fields(1).replaceAll("\n", "").replaceAll("\\s+", " "), credit - debit)
      }
    }.getOrElse(Seq())
  }

  def hasTransSearchMorePages(page: WebPage) = false

  def transSearchNextPage(page: WebPage) {} //No pagination supported

  def openAccountsHome(page: WebPage) {
    page.click("frame[name='leftmenu'] a[href*='myfinances']")
  }

  def isValidAccountsHomePage(page: WebPage) = {
    page.title == "Finances"
  }
}
