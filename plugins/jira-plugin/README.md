# Allure Jira Plugin

This plugin requited `execution.json` file in test result directory. 
This file contains information about report location for backlinks from Jira.  

## Export TestResult information

How it works:
* add issue link in your test cases (for example in java: `@Issue("ALLURE-1")`)
* enable plugin - add system property `-Dallure.jira.testresult.enabled=true`
* setup Jira: 
  * `-Dallure.jira.endpoint=https://<jira>/jira/rest/`
  * `-Dallure.jira.username=<username>`
  * `-Dallure.jira.password=<password>`
* generate report  

After that you will see such panel in Jira issue:

![TestResult Panel](img/testresult.png)

## Export Launch information

How it works:
* enable plugin - add system property `-Dallure.jira.launch.enabled=true`
* set issue for launch export - add system property `-Dallure.jira.launch.issue=ALLURE-2`
* setup Jira: 
  * `-Dallure.jira.endpoint=https://<jira>/jira/rest/`
  * `-Dallure.jira.username=<username>`
  * `-Dallure.jira.password=<password>`
* generate report  

After that you will see such panel in Jira issue:

![Launches Panel](img/launches.png)
