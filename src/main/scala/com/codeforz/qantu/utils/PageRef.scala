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

import com.codeforz.sclicks.WebPage
import java.io.File
import grizzled.slf4j.Logging

trait PageWrapper{
  def page:WebPage
  def isValid:Boolean
  def getError:Option[String]= if(isValid) None else Some("Invalid page")
}
/**
 * 
 */
class PageRef[T<:PageWrapper](private var page:T)extends Logging{

  def update[Q<:T](f:(WebPage)=>Q):Q={
    val newPage = f(page.page)
    newPage.getError match{
      case Some(e) =>
        val file = File.createTempFile(newPage.getClass.getName,".html")
        logger.error("Failed validating new page " + newPage + ", dumping page to " + file)
        newPage.page.saveTo(file.getAbsolutePath)
        sys.error(e)
      case _ =>
        page = newPage
        newPage
    }
  }

  def get = page

}