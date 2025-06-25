<#-- Versión corregida sin dependencia de locale -->
<#macro loginLayout displayInfo=false displayMessage=true>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title><#nested "title"></title>

    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    
    <#-- Cargar Font Awesome desde CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

     <#-- Cargar nuestro CSS personalizado -->
    <link href="${url.resourcesPath}/css/login.css" rel="stylesheet" />
</head>
<body>
    <#nested "form">

    <#-- JavaScript personalizado -->
    <script src="${url.resourcesPath}/js/login.js"></script>
    
</body>
</html>
</#macro>