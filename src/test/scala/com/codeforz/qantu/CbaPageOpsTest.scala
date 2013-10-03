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
 */

class CbaPageOpsTest extends SpecificationWithJUnit {
  val homePage = this.getClass.getResource("/cba/accounts-home.html").toURI.toURL.toString
  val transPage1 = this.getClass.getResource("/cba/trans-search-page.html").toURI.toURL.toString
  val transPage2 = this.getClass.getResource("/cba/last-trans-page.html").toURI.toURL.toString //default-trans-page.html
  val cbaOps = new CbaPageOps

  "CbaPageOps" should {
    "extract accts summary" in {
      cbaOps.getAccountsBalance(WebPage(homePage, false)) must not beEmpty
    }
    "detect paging" in {
      cbaOps.hasTransSearchMorePages(WebPage(transPage1, false)) must_== true
      cbaOps.hasTransSearchMorePages(WebPage(transPage2, false)) must_== true
    }
/*
    "extract transactions" in {
      val transactions = CbaPageOps.getTransactions(WebPage(transPage1, false))
      println(transactions)
      transactions must not beEmpty
    }
*/

    "extract accounts" in {
      val accounts = cbaOps.getAccountNames(WebPage(homePage, false))
      println(accounts)
      accounts must not beEmpty
    }

  }

}