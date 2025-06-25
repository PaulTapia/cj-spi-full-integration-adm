/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaAuthenticator 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.auth;

import ec.gob.funcionjudicial.user_storage.ConsejoJudicaturaStorageProvider;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.credential.CredentialInput;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.keycloak.models.credential.PasswordCredentialModel;

/**
 * -- AQUI AÑADIR LA DESCRIPCION DE LA CLASE --.
 *
 * <p>Historial de cambios:
 *
 * <ul>
 *   <li>1.0.0 - Descripción del cambio inicial - Danny.Tapia - 20/6/2025
 *       <!-- Añadir nuevas entradas de cambios aquí -->
 * </ul>
 *
 * @author Danny.Tapia
 * @version 1.0.0 $
 * @since 20/6/2025
 */
public class ConsejoJudicaturaAuthenticator implements Authenticator {

  private static final Logger logger = Logger.getLogger(ConsejoJudicaturaAuthenticator.class.getName());

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

    if (formData.containsKey("cancel")) {
      context.cancelLogin();
      return;
    }

    String organization = formData.getFirst("organization");
    String username = formData.getFirst("username");
    String password = formData.getFirst("password");
    String additionalData = formData.getFirst("additional-data");

    boolean requiresOrganization = "on".equals(additionalData);

    if (username == null || username.trim().isEmpty() || password == null) {
      context.challenge(context.form().createLoginUsernamePassword());
      return;
    }

    if (requiresOrganization && (organization == null || organization.trim().isEmpty())) {
      context.challenge(context.form().setError("Debe ingresar su organización").createLoginUsernamePassword());
      return;
    }

    KeycloakSession session = context.getSession();
    RealmModel realm = context.getRealm();
    UserModel user;

    if (requiresOrganization) {
      // Guardar organización en contexto para el SPI
      context.getAuthenticationSession().setUserSessionNote("organizacion", organization);

      // Buscar específicamente en nuestro SPI
      ConsejoJudicaturaStorageProvider provider = session.getProvider(
          ConsejoJudicaturaStorageProvider.class,
          "consejo-judicatura-user-storage"
      );

      if (provider == null) {
        logger.error("ConsejoJudicatura User Storage Provider no encontrado");
        context.challenge(context.form().setError("Error interno del sistema").createLoginUsernamePassword());
        return;
      }

      user = provider.getUserByUsernameAndOrganization(realm, username, organization);
    } else {
      // Buscar en LDAP/usuarios locales (excluir nuestro SPI)
      user = session.users().getUserByUsername(realm, username);
    }

    if (user == null) {
      logger.warn(String.format("Usuario '%s' no encontrado %s",
          username, requiresOrganization ? "en organización " + organization : ""));
      context.failure(AuthenticationFlowError.INVALID_USER);
      return;
    }

    if (!user.isEnabled()) {
      context.failure(AuthenticationFlowError.USER_DISABLED);
      return;
    }

    // Validar contraseña
    CredentialInput passwordCredential = UserCredentialModel.password(password);
    boolean passwordIsValid = session.userCredentialManager().isValid(realm, user, passwordCredential);

    if (!passwordIsValid) {
      context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
      return;
    }

    // Éxito
    context.setUser(user);
    context.success();
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    authenticate(context);
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