<#import "lib/template.ftl" as layout>
<@layout.loginLayout displayInfo=false; section>
    <#if section = "form">
        <div class="container">
            <div class="banner">
                <div class="banner-content">
                    <center>
                        <img src="${url.resourcesPath}/img/logo-judicatura.png" 
                            alt="Consejo de la Judicatura" 
                            class="logo-image">
                    </center>
                    <p>Sistema integrado de gestión judicial para una justicia moderna, transparente y accesible para todos los ciudadanos.</p>
                </div>
            </div>
            
            <div class="login-section">
                <div class="logo">
                    <div class="logo-icon">
                        <i class="fas fa-balance-scale"></i>
                    </div>
                    <div>
                        <div class="logo-text">Consejo de la Judicatura</div>
                        <div class="logo-subtext">Servicio Central de Autenticación</div>
                    </div>
                </div>
                
                <div class="header">
                    <h1>Inicio de Sesión</h1>
                    <p>Ingrese sus credenciales para acceder al sistema</p>
                </div>
                
                
                
                <form id="kc-form-login" action="${url.loginAction}" method="post">

                    <#if message?has_content>
                        <div class="alert alert-${message.type}">
                            ${message.summary}
                        </div>
                    </#if>
                    
                    <div class="important-note">
                        <strong>Importante</strong>
                        <div class="checkbox-container">
                            <input type="checkbox" id="additional-data" name="additional-data" value="on">
                            <label for="additional-data">Marque la casilla, si su perfil de usuario requiere un dato adicional para acceder a los servicios del Consejo de la Judicatura.</label>
                        </div>
                    </div>


                    <!-- Campo adicional que se mostrará solo cuando el checkbox esté marcado -->
                    <div class="additional-field" id="judicatura-field" style="display: none;">
                        <div class="form-group">
                            <label for="organization">Judicatura/Organización:</label>
                            <div class="input-with-icon">
                                <i class="fas fa-building"></i>
                                <input type="text" id="organization" name="organization"
                                       placeholder="Ingrese su organización" 
                                       autocomplete="organization">
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="username">Usuario:</label>
                        <div class="input-with-icon">
                            <i class="fas fa-user"></i>
                            <input type="text" id="username" name="username" 
                                   value="${(login.username!'')}" 
                                   placeholder="Ingrese su nombre de usuario" 
                                   required autofocus>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label for="password">Contraseña:</label>
                        <div class="input-with-icon">
                            <i class="fas fa-lock"></i>
                            <input type="password" id="password" name="password" 
                                   placeholder="Ingrese su contraseña" 
                                   required>
                        </div>
                    </div>
                    
                    <button type="submit" class="login-btn">INICIAR SESIÓN</button>
                </form>
                
                <div class="forgot-password">
                    <a href="#">¿Olvidó su contraseña?</a>
                </div>
                
                <div class="keycloak-info">
                    <i class="fas fa-shield-alt"></i>
                    <div>Autenticación segura proporcionada por Keycloak - Sistema de gestión de identidad y acceso</div>
                </div>
                
                <div class="footer">
                    Construyendo un servicio de justicia para la paz social
                </div>
            </div>
        </div>
    </#if>
</@layout.loginLayout>