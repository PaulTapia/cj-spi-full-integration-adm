/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaIdAdmBDMapper 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.mapper;

import ec.gob.funcionjudicial.dto.UsuarioFuncionJudicial;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

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
public class ConsejoJudicaturaAdmMapper extends AbstractOIDCProtocolMapper implements
    OIDCAccessTokenMapper,
    OIDCIDTokenMapper, UserInfoTokenMapper {

  public static final String PROVIDER_ID = "cj-mapper-usuarios-adm";
  private static final Logger logger = Logger.getLogger(ConsejoJudicaturaAdmMapper.class.getName());

  private static final List<ProviderConfigProperty> propiedadesConfiguracion = new ArrayList<>();

  static {
    OIDCAttributeMapperHelper.addTokenClaimNameConfig(propiedadesConfiguracion);
    OIDCAttributeMapperHelper.addIncludeInTokensConfig(propiedadesConfiguracion,
        ConsejoJudicaturaAdmMapper.class);
  }

  @Override
  protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
    // 1. Obtener la organización desde las notas de la sesión.
    String organizacion = userSession.getNote("organizacion");
    UserModel user = userSession.getUser();

    logger.info("setClaim: organizacion: " + organizacion);
    logger.info("setClaim: user: " + user);


  }

  @Override
  public String getDisplayCategory() {
    return "UsuarioFuncionJudicial internos y externo ADM";
  }

  @Override
  public String getDisplayType() {
    return "Roles y Menus para el UsiarioFuncionJudicial internos y externo ADM";
  }

  @Override
  public String getHelpText() {
    return "Añade roles y menus para el UsuarioFuncionJudicial internos y externo ADM del Consejo de la Judicatura";
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