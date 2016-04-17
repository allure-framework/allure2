<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Allure</title>
    <link rel="favicon" href="favicon.ico">
    <link rel="stylesheet" href="styles.css">
</head>
<body>
<div id="alert"></div>
<div id="content">
    <span class="spinner">
        <span class="spinner__circle"></span>
    </span>
</div>
<div id="popup"></div>
<script src="app.js"></script>
<#--TODO: load plugins here-->
<#--Plugins-->
<#list plugins as plugin>
    <!--<script src="plugins/${plugin}/script.js"></script>-->
</#list>
</body>
</html>
