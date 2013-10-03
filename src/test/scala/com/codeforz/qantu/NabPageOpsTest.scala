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
class NabPageOpsTest extends SpecificationWithJUnit {
  val home = WebPage.open(getClass.getResource("/nab/nab-home2.html").toString, false)
  val trans = WebPage.open(getClass.getResource("/nab/nab-trans-filter.html").toString, false)
  val transSearch = WebPage.open(getClass.getResource("/nab/nab-trans-search-result2.html").toString, false)
  val nabOps = new NabPageOps
  import nabOps._

  "Nab page ops" should{
    "parse account names" in{
      getAccountNames(home) must_== Seq("933324739")
    }
    "parse account balance" in{
      val balance = getAccountsBalance(home)
      println(balance)
      balance must haveSize(1)
    }
    "parse recent transactions" in{
      val trs = getTransactions(trans)
      trs foreach println
      trs must not beEmpty
      val trs2 = getTransactions(transSearch)
      trs2 foreach println
      trs2 must not beEmpty
    }
    "check paging" in{
      hasTransSearchMorePages(trans) must beFalse
      hasTransSearchMorePages(transSearch) must beTrue
    }
  }
}
