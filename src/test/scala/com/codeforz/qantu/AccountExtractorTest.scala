package com.codeforz.qantu

import org.specs2.mutable.SpecificationWithJUnit

/**
 * 
 */
class AccountExtractorTest extends SpecificationWithJUnit{
  "list implementations" in {
    AccountExtractor.supportedBanks must not beEmpty
  }

}
