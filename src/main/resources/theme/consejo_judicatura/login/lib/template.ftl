<#-- Template corregido sin dependencia de locale -->
<#macro loginLayout displayInfo=false displayMessage=true>
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="robots" content="noindex, nofollow">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title><#nested "title">Consejo de la Judicatura - Inicio de Sesión</title>

        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
            </#list>
        </#if>

        <#-- Cargar Font Awesome desde CDN -->
        <link href="${url.resourcesPath}/css/fontawesome/all.min.css" rel="stylesheet" />

        <#-- Cargar nuestro CSS personalizado -->
        <link href="${url.resourcesPath}/css/login.css" rel="stylesheet" />

        <#-- Favicon opcional -->
        <link rel="icon" type="image/x-icon" href="${url.resourcesPath}/img/favicon.ico">
    </head>
    <body>
    <#nested "form">

    <#-- JavaScript personalizado -->
    <script src="${url.resourcesPath}/js/login.js"></script>
    </body>
    </html>
</#macro>