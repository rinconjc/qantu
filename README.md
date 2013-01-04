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
val cbaExtractor = AccountExtractor.get("CBA")
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
If your bank is not supported you can either implement the connector following the below instructions or you can contact me to do it based on your scrambled accounts page.

-- TO BE CONTINUED ---
    
