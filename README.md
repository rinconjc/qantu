Qantu
=====

Qantu is a simple scala library to extract accounts data from online banking websites. It uses the [sclicks](https://github.com/julior/sclicks) library to programmatically interact with the websites and pull down the relevant data.

Currently, it only supports a few Australian banks, however adding support to other banks should be relatively easy given the existing framework and the provided interface (see Extending Qantu for more details).

Using Qantu
-------------------
Qantu uses Maven so include the following dependency in your pom.xml file:


```xml
<dependency>
    <groupId>com.codeforz</groupId>
    <artifactId>qantu</artifactId>
    <version>0.8-SNAPSHOT</version>
</dependency>
```    

The *AccountExtractor* class is the main entry point to the library:
* List the supported banks

```scala
import com.codeforz.qantu.AccountExtractor

val banks:Map[Stirng,String] = AccountExtractor.supportedBanks // will return a map of the currently supported banks

```

* Interact with a specific bank
    

```scala
val cbaExtractor = AccountExtractor("CBA")
//login before extracting any data. it will throw an error if credentials are invalid
cbaExtractor.login("<your_cba_client_id>", "<your_cba_password>") 
// print all your account names
cbaExtractor.accountNames foreach println  
// print all your accounts summary including balance
cbaExtractor.allAccountsBalance 
// search transactions for the given account in the given period.
cbaExtractor.retrieveTransactions("<your account name>", <start transaction date>, <end transaction date>) 
// extract all the historic transactions available for your account (~2 years for CBA)
cbaExtractor.retrieveAllTransactions("your_account_name") 
// returns the recent transactions for your account
cbaExtractor.retrieveRecentTransactions("<your_account_name>") 
//terminates the session
cbaExtractor.logoff() 
```

* Done!

Extending Qantu
----------------------------
If your bank is not supported you can implement the extractor following the below steps.
#### The basics first:
* You will need a valid internet account for the bank you want to implement the extractor for
* You may use Selenium IDE Firefox plugin to record the interactions with your bank website
* You need to download and save the following pages as HTML:
    * *Accounts summary page* The page that list your accounts including the balance amounts
    * *Transactions page* The page the list the recent transactions of one of your accounts
    * *Transactions search form* The page to search the transactions by date (if supported by the website)
    * *Transactions search result page* The search results page, in most cases will be the same a the normal transactions page.

#### Implement a new PageOps object 
* The PageOps trait defines a set of methods to navigate programmatically the bank website, as well as methods to extract relevant data. The website interaction is done using [sclicks](https://github.com/julior/sclicks), which provides JQuery like selectors to find and reference HTML element, and methods to interact with it. See the PageOps comments for more information and the existing PageOps implementations. 
* Unit test your PageOps implementation specially the data parsing and extractions methods using the pages you donwloaded above. (You should scramble the pages you downloaded above using the scala script [/src/test/resources/scrambler.sc](https://github.com/julior/qantu/blob/master/src/test/resources/scrambler.sc), before commiting to github)
* Run the integration test class *[TestExtractorHealthCheck](https://github.com/julior/qantu/blob/master/src/main/scala/com/codeforz/qantu/utils/ExtractorHealthCheck.scala)*, it will interactively ask for your bank details, and execute and verify each extractor method. If it succeeds you can commit/push your changes.



    
