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

import io.Source
import java.util.{Calendar, Date}
import com.codeforz.qantu.AccountExtractor

/**
 *
 */
trait ExtractorHealthCheck extends App{
  val Seq(bankId, cid,pwd) = Seq("Bank Id", "Client Id", "Password" ).map{f=> printf("\nEnter %s:",  f); Source.stdin.getLines().next()}

  AccountExtractor.get(bankId) foreach{extractor=>
    extractor.login(cid, pwd)
    val cal = Calendar.getInstance()
    extractor.allAccountsBalance.ensuring(_.size>0).foreach(println)
    val accounts = extractor.accountNames
    accounts.ensuring(!_.isEmpty).foreach(println)
    accounts.flatMap{account=>
      extractor.retrieveRecentTransactions(account)
    }.ensuring(!_.isEmpty).foreach(println)

    cal.add(Calendar.MONTH, -6)
    accounts.flatMap{account=>
      extractor.retrieveTransactions(account,cal.getTime, new Date)
    }.ensuring(!_.isEmpty).foreach(println)
    accounts.flatMap{account=>
      extractor.retrieveAllTransactions(account).take(40)
    }.ensuring(!_.isEmpty).foreach(println)

    println(bankId + " Health check completed with no errors...")
  }

}

object RuntimeExtractorHealthCheck extends ExtractorHealthCheck