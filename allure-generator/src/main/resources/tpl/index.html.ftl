<#-- @ftlvariable name="faviconUrl" type="java.lang.String" -->
<#-- @ftlvariable name="coreStyleUrls" type="java.lang.String[]" -->
<#-- @ftlvariable name="pluginStyleUrls" type="java.lang.String[]" -->
<#-- @ftlvariable name="coreJsUrls" type="java.lang.String[]" -->
<#-- @ftlvariable name="pluginJsUrls" type="java.lang.String[]" -->
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
    <meta name="allure-report-uuid" content="${reportUuid}">
    <title>${reportName!"Allure Report"}</title>
    <link rel="icon" href="${faviconUrl}">
    <!-- allure-core-head:start -->
    <#list coreStyleUrls as styleUrl>
    <link rel="stylesheet" type="text/css" href="${styleUrl}">
    </#list>
    <!-- allure-core-head:end -->
    <#list pluginStyleUrls as styleUrl>
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
    <!-- allure-core-body:start -->
    <script>
        window.__allureCoreLoaded = new Promise(function (resolve, reject) {
        window.__allureResolveCoreLoaded = resolve;
        window.__allureRejectCoreLoaded = reject;
    });
    </script>
    <#if coreJsUrls?has_content>
        <#list coreJsUrls as jsUrl>
    <script src="${jsUrl}"></script>
        </#list>
    <script>
        if (typeof window.__allureResolveCoreLoaded === "function") {
            window.__allureResolveCoreLoaded([]);
        }
    </script>
    <#else>
    <script>
        if (typeof window.__allureResolveCoreLoaded === "function") {
            window.__allureResolveCoreLoaded([]);
        }
    </script>
    </#if>
    <!-- allure-core-body:end -->
    <#if pluginJsUrls?has_content>
    <script>
        (function () {
            function loadScript(url) {
                return new Promise(function (resolve, reject) {
                    var script = document.createElement("script");
                    script.src = url;
                    script.onload = function () { resolve(url); };
                    script.onerror = function () { reject(new Error("Failed to load script " + url)); };
                    document.body.appendChild(script);
                });
            }

            var pluginScripts = [
                <#list pluginJsUrls as jsUrl>
                "${jsUrl}"<#sep>,</#sep>
                </#list>
            ];

            Promise.resolve(window.__allureCoreLoaded)
                .then(function () {
                    return pluginScripts.reduce(function (chain, scriptUrl) {
                        return chain.then(function () {
                            return loadScript(scriptUrl);
                        });
                    }, Promise.resolve());
                });
        })();
    </script>
    </#if>
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
