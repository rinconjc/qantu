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

package com.codeforz.qantu.model

import java.util.Date
import reflect.BeanProperty

/**
 *
 */

class AccountBalance(@BeanProperty val name:String, @BeanProperty val bsb:String
                     , @BeanProperty val number:String, @BeanProperty val balance: BigDecimal
                     , @BeanProperty val available: BigDecimal) {

  override def toString = ("CbaAcctSummary(acct:%s, bsb:%s, number:%s, balance:%f, " +
    "available:%f)").format(name, bsb, number, balance, available)
}


class AccountTransaction(@BeanProperty val date: Date, @BeanProperty val desc: String, @BeanProperty val amt: BigDecimal, @BeanProperty val pending:Boolean=false) {

  override def toString = "AccountTransaction(date:%tF, desc:%s, amt:%f)".format(date, desc, amt)

}

