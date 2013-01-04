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

import org.specs2.mutable.SpecificationWithJUnit

/**
 * 
 */
class CsvParserTest extends SpecificationWithJUnit{
  "CSV Parser" should{
    "parse exactly" in{
      val rows = CsvParser.parseFile("10/05/2012,\"test, description\",1234.34\n11/05/2012,\"test, description1\",-1234.34\n")
      rows foreach (x=>println(x.size))
      rows must haveSize(2)

      CsvParser.parseFile(
        """10/05/2012,"test, description",+333.34
          |11/05/2012,"test, description1",-4444.34""".stripMargin) must haveSize(2)
    }
  }

}
