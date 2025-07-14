/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaDirectGrantAuthenticator 14/7/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.auth;

import ec.gob.funcionjudicial.user_storage.ConsejoJudicaturaStorageProvider;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.credential.CredentialInput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

/**
 * -- AQUI AÑADIR LA DESCRIPCION DE LA CLASE --.
 *
 * <p>Historial de cambios:
 *
 * <ul>
 *   <li>1.0.0 - Descripción del cambio inicial - Danny.Tapia - 14/7/2025
 *       <!-- Añadir nuevas entradas de cambios aquí -->
 * </ul>
 *
 * @author Danny.Tapia
 * @version 1.0.0 $
 * @since 14/7/2025
 */
@SuppressWarnings("deprecation")
public class ConsejoJudicaturaDirectGrantAuthenticator implements Authenticator {

  private static final Logger logger = Logger.getLogger(
      ConsejoJudicaturaDirectGrantAuthenticator.class);

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    // Validar username
    if(context.getHttpRequest().getFormParameters().get("username") == null
        || context.getHttpRequest().getFormParameters().get("username").isEmpty()) {

      Response challenge = Response.status(400)
          .entity("{\"error\":\"invalid_request\",\"error_description\":\"No Username\"}")
          .header("Content-Type", "application/json")
          .build();
      context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
      return;
    }

    // Validar password
    if(context.getHttpRequest().getFormParameters().get("password") == null
        || context.getHttpRequest().getFormParameters().get("password").isEmpty()) {

      Response challenge = Response.status(400)
          .entity("{\"error\":\"invalid_request\",\"error_description\":\"No Password\"}")
          .header("Content-Type", "application/json")
          .build();
      context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
      return;
    }

    String username = context.getHttpRequest().getFormParameters().getFirst("username").trim();
    String password = context.getHttpRequest().getFormParameters().getFirst("password").trim();
    String organization = context.getHttpRequest().getFormParameters().getFirst("organization");

    UserModel user = findUser(context, username, organization);

    // Usuario no encontrado
    if (user == null) {
      logger.warn("Usuario no encontrado: " + username);
      Response challenge = Response.status(400)
          .entity("{\"error\":\"invalid_request\",\"error_description\":\"User Not Found\"}")
          .header("Content-Type", "application/json")
          .build();
      context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
      return;
    }

    // Usuario deshabilitado
    if (!user.isEnabled()) {
      Response challenge = Response.status(400)
          .entity("{\"error\":\"invalid_request\",\"error_description\":\"User Disabled\"}")
          .header("Content-Type", "application/json")
          .build();
      context.failureChallenge(AuthenticationFlowError.USER_DISABLED, challenge);
      return;
    }

    // Validar contraseña
    CredentialInput passwordCredential = UserCredentialModel.password(password);
    //boolean valid = context.getSession().userCredentialManager()
    //    .isValid(context.getRealm(), user, passwordCredential);

    boolean valid = user.credentialManager().isValid(passwordCredential);

    if (!valid) {
      Response challenge = Response.status(400)
          .entity("{\"error\":\"invalid_request\",\"error_description\":\"Invalid Credentials\"}")
          .header("Content-Type", "application/json")
          .build();
      context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
      return;
    }

    // Guardar organización si existe
    if (organization != null && !organization.trim().isEmpty()) {
      context.getAuthenticationSession().setUserSessionNote("organizacion", organization);
    }

    // Configurar usuario y éxito
    context.setUser(user);
    context.success();
  }

  private UserModel findUser(AuthenticationFlowContext context, String username, String organization) {
    KeycloakSession session = context.getSession();
    RealmModel realm = context.getRealm();

    if (organization != null && !organization.trim().isEmpty()) {
      // Buscar con organización
      ConsejoJudicaturaStorageProvider provider = session.getProvider(
          ConsejoJudicaturaStorageProvider.class, "cj-storage-adm");

      if (provider != null) {
        return provider.getUserByUsernameAndOrganization(realm, username, organization);
      }
    }

    // Buscar en LDAP/usuarios locales primero (excluir nuestro SPI)
    UserModel user = session.users().getUserByUsername(realm, username);
    if (user == null) {
      // Si no se encuentra en LDAP, buscar en nuestro SPI
      ConsejoJudicaturaStorageProvider provider = session.getProvider(
          ConsejoJudicaturaStorageProvider.class, "cj-storage-adm");
      if (provider != null) {
        user = provider.getUserByUsername(username, realm);
      }
    }

    return user;
  }


  @Override
  public void action(AuthenticationFlowContext context) {
    // No action needed for direct grant
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    // No required actions
  }

  @Override
  public void close() {
    // No cleanup needed
  }

}