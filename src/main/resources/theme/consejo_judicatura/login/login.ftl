<#import "lib/template.ftl" as layout>
<@layout.loginLayout displayInfo=false; section>
    <#if section = "form">
        <div class="container">
            <div class="banner">
                <div class="banner-content">
                    <center>
                        <img src="${url.resourcesPath}/img/logo-judicatura.png"
                             alt="${msg("loginAccountTitle")}"
                             class="logo-image">
                    </center>
                    <p>${msg("login.description")}</p>
                </div>
            </div>

            <div class="login-section">
                <div class="logo">
                    <div class="logo-icon">
                        <i class="fas fa-balance-scale"></i>
                    </div>
                    <div>
                        <div class="logo-text">${msg("loginAccountTitle")}</div>
                        <div class="logo-subtext">${msg("login.subtitle")}</div>
                    </div>
                </div>

                <div class="header">
                    <h1>${msg("loginTitle")}</h1>
                    <p>${msg("login.instructions")}</p>
                </div>

                <form id="kc-form-login" action="${url.loginAction}" method="post">

                    <#if message?has_content>
                        <div class="alert alert-${message.type}">
                            ${message.summary}
                        </div>
                    </#if>

                    <div class="important-note">
                        <strong>${msg("login.important")}</strong>
                        <div class="checkbox-container">
                            <input type="checkbox" id="additional-data" name="additional-data" value="on">
                            <label for="additional-data">${msg("login.additionalData")}</label>
                        </div>
                    </div>

                    <!-- Campo adicional que se mostrará solo cuando el checkbox esté marcado -->
                    <div class="additional-field" id="judicatura-field">
                        <div class="form-group">
                            <label for="organization">${msg("login.organization")}:</label>
                            <div class="input-with-icon">
                                <i class="fas fa-building"></i>
                                <input type="text" id="organization" name="organization"
                                       placeholder="${msg("login.organizationPlaceholder")}"
                                       autocomplete="organization">
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="username">${msg("username")}:</label>
                        <div class="input-with-icon">
                            <i class="fas fa-user"></i>
                            <input type="text" id="username" name="username"
                                   value="${(login.username!'')}"
                                   placeholder="${msg("login.usernamePlaceholder")}"
                                   required autofocus>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password">${msg("password")}:</label>
                        <div class="input-with-icon">
                            <i class="fas fa-lock"></i>
                            <input type="password" id="password" name="password"
                                   placeholder="${msg("login.passwordPlaceholder")}"
                                   required>
                        </div>
                    </div>

                    <button type="submit" class="login-btn">${msg("doLogIn")}</button>
                </form>

                <div class="forgot-password">
                    <a href="#">${msg("doForgotPassword")}</a>
                </div>

                <div class="keycloak-info">
                    <i class="fas fa-shield-alt"></i>
                    <div>${msg("login.keycloakInfo")}</div>
                </div>

                <div class="footer">
                    ${msg("login.footer")}
                </div>
            </div>
        </div>
    </#if>
</@layout.loginLayout>