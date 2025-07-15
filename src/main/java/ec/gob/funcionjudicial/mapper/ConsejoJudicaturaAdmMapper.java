/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaIdAdmBDMapper 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.mapper;

import ec.gob.funcionjudicial.dto.UsuarioFuncionJudicial;
import ec.gob.funcionjudicial.enums.TipoUsuario;
import ec.gob.funcionjudicial.model.Organizacion;
import ec.gob.funcionjudicial.model.Recurso;
import ec.gob.funcionjudicial.model.Usuario;
import ec.gob.funcionjudicial.model.UsuarioExterno;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.jboss.logging.Logger;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
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

  private EntityManager getEntityManager(KeycloakSession session) {
    return session.getProvider(JpaConnectionProvider.class, "adm").getEntityManager();
  }

  @Override
  protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
      UserSessionModel userSession, KeycloakSession keycloakSession,
      ClientSessionContext clientSessionCtx) {
    try {
      String organizacion = userSession.getNote("organizacion");
      UserModel user = userSession.getUser();
      String username = user.getUsername();

      logger.info("setClaim: organizacion: " + organizacion + ", user: " + username);

      UsuarioFuncionJudicial usuarioFJ = obtenerUsuarioCompleto(username, organizacion, keycloakSession);

      if (usuarioFJ != null) {
        // Agregar el objeto completo como UsuarioFuncionJudicial
        token.getOtherClaims().put("UsuarioFuncionJudicial", usuarioFJ);
        logger.info("Usuario agregado al token: " + username);
      } else {
        logger.warn("UsuarioFuncionJudicial es null para: " + username);
      }

    } catch (Exception e) {
      logger.error("Error al procesar usuario en token", e);
    }
  }

  private UsuarioFuncionJudicial obtenerUsuarioCompleto(String username, String institucion,
      KeycloakSession session) {
    try {
      EntityManager em = getEntityManager(session);

      Usuario usuario = buscarUsuarioPorUsername(em, username);
      if (usuario == null) {
        logger.warn("Usuario no encontrado: " + username);
        return null;
      }

      Boolean primerIngreso = usuario.getPrimerIngreso();
      Boolean claveCaducada = usuario.getCaducado();

      return llenarUsuario(username, institucion, primerIngreso, claveCaducada, usuario, em);

    } catch (Exception e) {
      logger.error("Error al obtener usuario completo", e);
      return null;
    }
  }

  private Usuario buscarUsuarioPorUsername(EntityManager em, String username) {
    try {
      String hql = "select u from Usuario u where u.username = :username";
      Query q = em.createQuery(hql);
      q.setParameter("username", username);
      return (Usuario) q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      logger.error("Error al buscar usuario: " + username, e);
      return null;
    }
  }

  private UsuarioFuncionJudicial llenarUsuario(String username, String institucion,
      Boolean primerIngreso, Boolean claveCaducada, Usuario usuario, EntityManager em) {

    if (usuario == null) return null;

    try {
      UsuarioFuncionJudicial user = usuario.toUsuarioFuncionJudicial();
      user.setIdUsuario(usuario.getId());
      user.setUsuarioAdicional(institucion);

      List<ec.gob.funcionjudicial.model.Rol> roles = listarRoles(em, usuario, institucion);
      user.setRoles(new ArrayList<>());
      List<Long> allowedOpciones = new ArrayList<>();

      for (ec.gob.funcionjudicial.model.Rol rol : roles) {
        for (Recurso recurso : rol.getRecursos()) {
          user.getPermisos().add(recurso.toPermiso());
          if (recurso.getMenu() != null && recurso.getMenu()) {
            allowedOpciones.add(recurso.getId());
          }
        }
        user.getRoles().add(rol.toIRol());
      }

      List<Recurso> aplicaciones = listarRecursosPorIds(em, allowedOpciones);
      if (aplicaciones != null) {
        for (Recurso aplicacion : aplicaciones) {
          if ("APP".equals(aplicacion.getTipo())) {
            user.getOpciones().add(aplicacion.toOpcion(allowedOpciones));
          }
        }
      }

      if (institucion != null && !institucion.isEmpty()) {
        Organizacion organizacion = obtenerOrganizacionPorAcronimo(em, institucion);
        if (organizacion != null) {
          user.setIdOrganizacion(organizacion.getId());
        }
      }

      user.setPrimerIngreso(primerIngreso);
      user.setClaveCaducada(claveCaducada);
      user.setTipoUsuario(determinarTipoUsuario(usuario));

      return user;
    } catch (Exception e) {
      logger.error("Error al llenar usuario", e);
      return null;
    }
  }

  private List<ec.gob.funcionjudicial.model.Rol> listarRoles(EntityManager em, Usuario usuario, String institucion) {
    try {
      String hql = "select distinct ur.rol from UsuarioRol ur " +
          "left join fetch ur.rol.recursos " +
          "where ur.usuario=:usuario and ur.estado=:estado";
      if (institucion == null || institucion.isEmpty()) {
        hql += " and ur.organizacion is null";
      } else {
        hql += " and ur.organizacion.acronimo=:institucion";
      }

      Query q = em.createQuery(hql);
      q.setParameter("usuario", usuario);
      q.setParameter("estado", "ACT");

      if (institucion != null && !institucion.isEmpty()) {
        q.setParameter("institucion", institucion);
      }

      return q.getResultList();
    } catch (Exception e) {
      logger.error("Error al listar roles", e);
      return new ArrayList<>();
    }
  }

  private List<Recurso> listarRecursosPorIds(EntityManager em, List<Long> allowedOpciones) {
    try {
      if (allowedOpciones.isEmpty()) {
        return new ArrayList<>();
      }

      String hql = "select distinct r from Recurso r where r.id in :allowedOpciones";
      Query q = em.createQuery(hql);
      q.setParameter("allowedOpciones", allowedOpciones);

      return q.getResultList();
    } catch (Exception e) {
      logger.error("Error al listar recursos", e);
      return new ArrayList<>();
    }
  }

  private Organizacion obtenerOrganizacionPorAcronimo(EntityManager em, String institucion) {
    try {
      String hql = "select o from Organizacion o where o.acronimo=:institucion";
      Query q = em.createQuery(hql);
      q.setParameter("institucion", institucion);
      return (Organizacion) q.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      logger.error("Error al buscar organización", e);
      return null;
    }
  }

  private TipoUsuario determinarTipoUsuario(Usuario usuario) {
    if (usuario instanceof UsuarioExterno) {
      return TipoUsuario.EXTERNO;
    }
    return TipoUsuario.INTERNO;
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
    return propiedadesConfiguracion;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

}