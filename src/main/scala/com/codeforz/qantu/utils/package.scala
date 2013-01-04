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

import java.text.{SimpleDateFormat, DecimalFormat, ParseException}
import java.math.{BigDecimal => JBigDec}
import scala.util.control.Exception._
import org.apache.commons.logging.LogFactory
import annotation.tailrec

/**
  *
 */
package object utils {
  val log = LogFactory.getLog(this.getClass.getPackage.getName)
  val decFormats =  Stream("#,##0.00CR;#,##0.00DR","#,##0.00;-#,##0.00")
  val BLANK_CHARS = "^[\\p{Z}\r\n]+|[\\p{Z}\r\n]+$"

  implicit def toBigDecimal(str:String):BigDecimal = parseDecimal(str)

  def parseDecimal(value: String): BigDecimal = {
    val cleanValue = value.replaceAll("[\\$\\s\\+]","")//remove $, spaces and + chars
    if (cleanValue.isEmpty) JBigDec.ZERO
    else decFormats.map(f => catching(classOf[ParseException]).opt(new DecimalFormat(f) {
      setParseBigDecimal(true)
    }.parse(cleanValue).asInstanceOf[JBigDec])).find(_.isDefined).map(_.get).getOrElse{
      sys.error("Failed parsing " + cleanValue)
    }
  }

  def cleanText(str:String)=str.replaceAll("\n","").replaceAll("\\p{Z}+"," ").trim

  def removeBlanks(str:String) = str.replaceAll(BLANK_CHARS, "")

  def toDate(str:String, fmt:String) = new SimpleDateFormat(fmt).parse(str)

  @tailrec
  def waitUntil(cond: =>Boolean, maxSecs:Int, waitSecs:Int=2){
    if (!cond){
      if (maxSecs<=0){
        log.error("Gave up waiting ...")
      }else{
        log.warn("Waiting for condition ...")
        Thread.sleep(waitSecs*1000)
        waitUntil(cond, maxSecs - waitSecs)
      }
    }
  }

}
