<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Allure Report</title>
    <#list plugins as plugin>
        <#list plugin.cssFiles as cssFile>
            <link rel="stylesheet" href="plugins/${plugin.id}/${cssFile}">
        </#list>
    </#list>
</head>
<body>
<div id="app"></div>
<script src="app.js"></script>
<#list plugins as plugin>
    <#list plugin.jsFiles as jsFile>
    <script src="plugins/${plugin.id}/${jsFile}"></script>
    </#list>
</#list>
</body>
</html>
