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

package com.codeforz.qantu.utils

import scala.util.parsing.combinator._

// A CSV parser based on RFC4180
// http://tools.ietf.org/html/rfc4180

object CsvParser extends RegexParsers {
  override val skipWhitespace = false   // meaningful spaces in CSV

  private def COMMA   = ","
  private def DQUOTE  = "\""
  private def DQUOTE2 = "\"\"" ^^ { case _ => "\"" }  // combine 2 dquotes into 1
  private def CRLF    = "\r\n" | "\n"
  private def TXT     = "[^\",\r\n]".r
  private def SPACES  = "[ \t]+".r
  private def EMPTY_LINE = "^$".r

  private def file: Parser[List[List[String]]] = repsep(record, CRLF) <~ opt(CRLF)
  private def record: Parser[List[String]] = not(EMPTY_LINE) ~> repsep(field, COMMA)
  private def field: Parser[String] = escaped|nonescaped

  private def escaped: Parser[String] = {
    ((SPACES?)~>DQUOTE~>((TXT|COMMA|CRLF|DQUOTE2)*)<~DQUOTE<~(SPACES?)) ^^ {
      case ls => ls.mkString("")
    }
  }

  private def nonescaped: Parser[String] = (TXT*) ^^ { case ls => ls.mkString("") }

  def parseFile(s: String) = parseAll(file, s) match {
    case Success(res, _) => res
    case e => throw new Exception(e.toString)
  }

  def parseLine(s:String)=parseAll(record, s) match {
    case Success(res, _) => res
    case e => sys.error("Failed parsing line:" + s)
  }
}
