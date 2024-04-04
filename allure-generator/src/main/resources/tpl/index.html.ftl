<#-- @ftlvariable name="faviconUrl" type="java.lang.String" -->
<#-- @ftlvariable name="stylesUrls" type="java.lang.String[]" -->
<#-- @ftlvariable name="jsUrls" type="java.lang.String[]" -->
<#-- @ftlvariable name="reportDataFiles" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="analyticsDisable" type="boolean" -->
<#-- @ftlvariable name="allureVersion" type="java.lang.String" -->
<#-- @ftlvariable name="reportUuid" type="java.lang.String" -->
<#-- @ftlvariable name="reportName" type="java.lang.String" -->
<#-- @ftlvariable name="reportLanguage" type="java.lang.String" -->
<!DOCTYPE html>
<html dir="ltr" lang="${reportLanguage!"en"}">
<head>
    <meta charset="utf-8">
    <title>${reportName!"Allure Report"}</title>
    <link rel="icon" href="${faviconUrl}">
    <#list stylesUrls as styleUrl>
    <link rel="stylesheet" type="text/css" href="${styleUrl}">
    </#list>
</head>
<body>
    <div id="alert"></div>
    <div id="content">
        <span class="spinner">
            <span class="spinner__circle"></span>
        </span>
    </div>
    <div id="popup"></div>
    <#list jsUrls as jsUrl>
    <script src="${jsUrl}"></script>
    </#list>
    <#if analyticsDisable == false>
    <script async src="https://www.googletagmanager.com/gtag/js?id=G-FVWC4GKEYS"></script>
    </#if>
    <script>
        window.dataLayer = window.dataLayer || [];
        function gtag(){dataLayer.push(arguments);}
        gtag('js', new Date());
        gtag('config', 'G-FVWC4GKEYS', {
          'allureVersion': '${allureVersion}',
          'reportUuid': '${reportUuid}',
          'single_file': ${reportDataFiles?has_content?string}
        });
    </script>
    <#if reportDataFiles?has_content>
    <script async>
        window.reportDataReady = false;
        window.reportData = window.reportData || {};
        function d(name, value){
            return new Promise(function (resolve) {window.reportData[name] = value;resolve(true)});
        }
    </script>
    <script defer>
      Promise.allSettled([
        <#list reportDataFiles as name, value>
        d('${name}','${value}'),
        </#list>
      ]).then(function(){window.reportDataReady = true;})
    </script>
    </#if>
</body>
</html>
