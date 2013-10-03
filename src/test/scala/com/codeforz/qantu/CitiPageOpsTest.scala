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

import org.specs2.mutable.SpecificationWithJUnit
import com.codeforz.sclicks.WebPage

/**
 * 
 */
class CitiPageOpsTest extends SpecificationWithJUnit {
  lazy val page = WebPage.open(getClass.getResource("/citi/citi-home-latin.html").toURI.toString)
  val citiOps = new CitiPageOps
  import citiOps._
  "parse balance" in{
    val balance = getAccountsBalance(page)
    println(balance)
    balance must not beEmpty
  }

}
