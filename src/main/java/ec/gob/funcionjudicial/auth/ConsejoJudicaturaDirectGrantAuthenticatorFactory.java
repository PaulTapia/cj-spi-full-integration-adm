/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaDirectGrantAuthenticatorFactory 14/7/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.auth;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

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
public class ConsejoJudicaturaDirectGrantAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "consejo-judicatura-direct-grant";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Consejo Judicatura Direct Grant";
    }

    @Override
    public String getHelpText() {
        return "Direct grant authenticator con soporte de organización para Consejo de la Judicatura.";
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ConsejoJudicaturaDirectGrantAuthenticator();
    }

    @Override
    public String getReferenceCategory() {
        return "password";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}