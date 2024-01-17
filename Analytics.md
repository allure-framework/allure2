## Allure Report's Anonymous usage statistics

Allure report has begun gathering anonymous aggregate user
behaviour analytics and reporting these to Google Analytics. 

### Why?

Allure report is provided free of charge and we don't have direct
communication with its users nor time resources to ask directly for
their feedback. As result, we now use anonymous aggregate user 
analytics to help us understand how Allure Report is being used.

### When/Where?

Allure's analytics are sent throughout Allure report's plugins to Google Analytics over HTTPS.

### How?
    
The code is viewable in [GaPlugin.java](https://github.com/allure-framework/allure2/blob/main/allure-generator/src/main/java/io/qameta/allure/ga/GaPlugin.java)

### Opting out

Allure analytics helps us maintainers and leaving it on is appreciated. However, if you want to opt out of 
Allure's analytics, you can set this variable in your environment:

```$xslt
export ALLURE_NO_ANALYTICS=1
``` 
