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
class AnzPageOpsTest extends SpecificationWithJUnit {
  lazy val transPage = WebPage.open(getClass.getResource("/anz/anz-trans2-latin.html").toURI.toString)
  lazy val acctsPage = WebPage.open(getClass.getResource("/anz/anz-home2.html").toString)
  import AnzPageOps._

  "AnzPageOps" should{
    "get accounts balance" in{
      val balances = getAccountsBalance(acctsPage)
      println(balances)
      balances must haveSize(2)
    }
    "get account names" in{
      getAccountNames(acctsPage) must_== Seq("Access Cheque Account", "Progress Saver")
    }
  }

  "Parse transactions" in{
    val trans = getTransactions(transPage)
    println(trans)
    trans.foreach(_.amt mustNotEqual(BigDecimal(0)))
    trans must haveSize(7)
  }

}
