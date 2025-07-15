/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaAuthenticator 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.auth;

import ec.gob.funcionjudicial.user_storage.ConsejoJudicaturaStorageProvider;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.models.*;

import javax.ws.rs.core.MultivaluedMap;

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
public class ConsejoJudicaturaAuthenticator extends UsernamePasswordForm implements Authenticator {

  private static final Logger logger = Logger.getLogger(ConsejoJudicaturaAuthenticator.class.getName());

  @Override
  protected Response challenge(AuthenticationFlowContext context, String error, String field) {
    setupFormAttributes(context);
    return super.challenge(context, error, field);
  }

  @Override
  protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
    setupFormAttributes(context);
    return super.challenge(context, formData);
  }

  @Override
  protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
    // Validar username/password básico primero
    boolean basicValidation = super.validateForm(context, formData);

    if (!basicValidation) {
      return false;
    }

    // Validación adicional para organización
    String additionalData = formData.getFirst("additional-data");
    String organization = formData.getFirst("organization");
    boolean requiresOrganization = "on".equals(additionalData);

    return !requiresOrganization || (organization != null && !organization.trim().isEmpty());
  }

  @Override
  public boolean validateUser(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
    String username = inputData.getFirst("username");
    String organization = inputData.getFirst("organization");
    String additionalData = inputData.getFirst("additional-data");
    boolean requiresOrganization = "on".equals(additionalData);

    try {
      UserModel user = findUser(context, username, organization, requiresOrganization);

      if (user == null) {
        logger.warn(String.format("Usuario '%s' no encontrado %s",
            username, requiresOrganization ? "en organización " + organization : ""));
        return false;
      }

      if (!user.isEnabled()) {
        context.getEvent().user(user);
        context.getEvent().error("user_disabled");
        return false;
      }

      // Guardar organización en sesión
      if (requiresOrganization && organization != null) {
        context.getAuthenticationSession().setUserSessionNote("organizacion", organization);
      }

      context.setUser(user);
      context.success();
      return true;

    } catch (Exception e) {
      logger.error("Error al buscar usuario: " + e.getMessage(), e);
      return false;
    }
  }

  private UserModel findUser(AuthenticationFlowContext context, String username,
      String organization, boolean requiresOrganization) {

    KeycloakSession session = context.getSession();
    RealmModel realm = context.getRealm();

    if (requiresOrganization) {
      ConsejoJudicaturaStorageProvider provider = session.getProvider(
          ConsejoJudicaturaStorageProvider.class,
          "cj-storage-adm"
      );

      if (provider == null) {
        logger.error("ConsejoJudicatura User Storage Provider no encontrado");
        return null;
      }

      return provider.getUserByUsernameAndOrganization(realm, username, organization);
    } else {
      // Buscar en LDAP/usuarios locales primero
      UserModel user = session.users().getUserByUsername(realm, username);

      if (user == null) {
        ConsejoJudicaturaStorageProvider provider = session.getProvider(
            ConsejoJudicaturaStorageProvider.class,
            "cj-storage-adm"
        );

        if (provider != null) {
          user = provider.getUserByUsername(username, realm);
        }
      }

      return user;
    }
  }

  private void setupFormAttributes(AuthenticationFlowContext context) {
    // Configurar atributos para el template personalizado
    context.form().setAttribute("showOrganizationField", true);
    context.form().setAttribute("organizationLabel", "Organización");
    context.form().setAttribute("additionalDataLabel", "Soy usuario del Consejo de la Judicatura");
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