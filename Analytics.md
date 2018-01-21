## Allure Report's Anonymous usage statistics

Allure report has begun gathering anonymous aggregate user
behaviour analytics and reporting these to Google Analytics. 

### Why?

Allure report is provided free of charge and we don't have direct
communication with its users nor time resources to ask directly for
their feedback. As result, we now use anonymous aggregate user 
analytics to help us understand how Allure Report is being used.

### What?

Allure's analytics record some shared information for
every event:

- The Google Analytics version i.e. `1` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#v)
- The Allure report analytics tracking ID e.g. `UA-88115679-3` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#tid)
- The Allure report version e.g. `2.2.1` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#av)
- The type of CI system used e.g. `Jenkins` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#cd_)
- The Google Analytics anonymous IP setting is enabled i.e. `1` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#aip)
- The count of test results e.g `21` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#cm_)
- The count of plugins used e.g. `11` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#cm_)
- The test framework used e.g. `TestNG` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#cm_)
- The language used e.g. `Java` (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#cm_)
- The Allure report analytics hit type is event (https://developers.google.com/analytics/devguides/collection/protocol/v1/parameters#t)

With the recorded information, it is not possible for us to match any particular real user with the anonymized user ID. 

### When/Where?

Allure's analytics are sent throughout Allure report's plugins to Google Analytics over HTTPS.

### How?
    
The code is viewable in [GaPlugin.java](https://github.com/allure-framework/allure2/blob/master/allure-generator/src/main/java/io/qameta/allure/ga/GaPlugin.java)

### Opting out

Allure analytics helps us maintainers and leaving it on is appreciated. However, if you want to opt out of 
Allure's analytics, you can set this variable in your environment:

```$xslt
export ALLURE_NO_ANALYTICS=1
``` 
