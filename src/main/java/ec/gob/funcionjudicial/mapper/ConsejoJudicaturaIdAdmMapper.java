/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaIdAdmBDMapper 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.mapper;

import java.util.List;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;

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
public class ConsejoJudicaturaIdAdmMapper extends AbstractOIDCProtocolMapper implements
    OIDCAccessTokenMapper,
    OIDCIDTokenMapper, UserInfoTokenMapper {

  public static final String PROVIDER_ID = "cj-mapper-usuarios-ext-adm";

  @Override
  public String getDisplayCategory() {
    return "";
  }

  @Override
  public String getDisplayType() {
    return "";
  }

  @Override
  public String getHelpText() {
    return "";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return List.of();
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}