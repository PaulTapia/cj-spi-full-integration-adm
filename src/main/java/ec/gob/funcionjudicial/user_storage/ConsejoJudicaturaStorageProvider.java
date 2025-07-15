/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaStorageProvider 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.user_storage;

import ec.gob.funcionjudicial.model.HistoricoAccesos;
import ec.gob.funcionjudicial.model.Usuario;
import ec.gob.funcionjudicial.utils.MD5Util;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.keycloak.storage.user.UserQueryProvider;

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
@Slf4j
public class ConsejoJudicaturaStorageProvider implements
    UserStorageProvider,
    UserLookupProvider,
    CredentialInputValidator,
    UserQueryProvider {

  private static final Logger logger = Logger.getLogger(ConsejoJudicaturaStorageProvider.class);

  @Setter
  @Getter
  private KeycloakSession session;
  @Setter
  @Getter
  private ComponentModel model;

  private final Map<String, UserModel> userCache = new ConcurrentHashMap<>();

  private EntityManager getEntityManager() {
    return session.getProvider(JpaConnectionProvider.class, "adm").getEntityManager();
  }

  @Override
  public boolean supportsCredentialType(String s) {
    return CredentialModel.PASSWORD.equals(s);
  }

  @Override
  public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String s) {
    return supportsCredentialType(s);
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public boolean isValid(RealmModel realmModel, UserModel userModel,
      CredentialInput credentialInput) {
    if (!supportsCredentialType(credentialInput.getType()) || !(credentialInput instanceof UserCredentialModel)) {
      return false;
    }

    UserCredentialModel cred = (UserCredentialModel) credentialInput;
    String username = userModel.getUsername();
    String acronimo = getOrganizacionFromContext();

    try {
      Usuario usuario = null;

      if (acronimo != null && !acronimo.isEmpty()) {
        TypedQuery<Usuario> query = getEntityManager().createQuery(
            "SELECT u FROM UsuarioExterno u JOIN u.usuarioRoles ur JOIN ur.organizacion o WHERE u.username = :username AND o.acronimo = :acronimo AND u.estado = 'ACT' AND ur.estado = 'ACT'", Usuario.class);
        query.setParameter("username", username);
        query.setParameter("acronimo", acronimo);
        usuario = query.getResultStream().findFirst().orElse(null);
      } else {
        TypedQuery<Usuario> query = getEntityManager().createQuery(
            "SELECT u FROM UsuarioExterno u WHERE u.username = :username AND u.estado = 'ACT'",
            Usuario.class);
        query.setParameter("username", username);
        usuario = query.getResultStream().findFirst().orElse(null);
      }

      if (usuario != null) {
        String hashedPassword = MD5Util.hash(cred.getChallengeResponse());
        boolean isValid = hashedPassword.equals(usuario.getPassword());

        if (isValid) {
          registrarAcceso(usuario, true, acronimo);
        }

        return isValid;
      }
    } catch (Exception e) {
      logger.error("Error validating user", e);
    }
    return false;
  }

  /**
   * Obtiene la organización del contexto de autenticación.
   */
  private String getOrganizacionFromContext() {
    try {
      AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
      if (authSession != null) {
        //return authSession.getUserSessionNotes().get("organizacion");
        return authSession.getAuthNote("organizacion");
      }
    } catch (Exception e) {
      // Log error
    }
    return null;
  }


  private void registrarAcceso(Usuario usuario, boolean exitoso, String acronimo) {
    EntityTransaction tx = null;
    try {
      tx = getEntityManager().getTransaction();
      tx.begin();

      HistoricoAccesos acceso = new HistoricoAccesos();
      acceso.setUsuario(usuario);
      acceso.setEstado(exitoso ? "OK" : "NO");
      acceso.setFechaInicio(LocalDateTime.now());
      acceso.setFechaFin(LocalDateTime.now());
      acceso.setUsuarioRegistra(usuario.getUsername());
      acceso.setUsuarioActualiza(usuario.getUsername());
      acceso.setUsername(usuario.getUsername());
      acceso.setIp(getClientIP());

      getEntityManager().persist(acceso);
      tx.commit();
    } catch (Exception e) {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      logger.error("Error registrando acceso", e);
    }
  }

  private String getClientIP() {
    try {
      // Headers comunes de F5
      String[] headers = {
          "X-Forwarded-For",
          "X-Real-IP",
          "X-Client-IP",
          "X-Cluster-Client-IP",
          "True-Client-IP"
      };

      for (String header : headers) {
        String ip = session.getContext().getRequestHeaders().getHeaderString(header);
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
          logger.info("IP obtenida de header " + header + ": " + ip);
          return ip.split(",")[0].trim();
        }
      }

      return session.getContext().getConnection().getRemoteAddr();
    } catch (Exception e) {
      return "unknown";
    }
  }

  @Override
  public void close() {
  }

  @Override
  public UserModel getUserById(String s, RealmModel realmModel) {
    logger.info("getUserById called with id: " + s);

    if (StorageId.isLocalStorage(s)) {
      logger.info("Local storage ID, returning null");
      return null;
    }

    String externalId = StorageId.externalId(s);
    logger.info("External ID extracted: " + externalId);

    UserModel user = getUserByUsername(externalId, realmModel);
    logger.info("User found: " + (user != null));


    return user;
  }

  @Override
  public UserModel getUserByUsername(String s, RealmModel realmModel) {
    try {
      TypedQuery<Usuario> query = getEntityManager().createQuery(
          "SELECT u FROM UsuarioExterno u WHERE u.username = :username AND u.estado = 'ACT'",
          Usuario.class);
      query.setParameter("username", s);
      Usuario usuario = query.getResultStream().findFirst().orElse(null);

      if (usuario != null) {
        return new ConsejoJudicaturaAdapter(session, realmModel, model, usuario);
      }
    } catch (Exception e) {
      logger.error("Error querying user", e);
    }
    return null;
  }

  // Método específico para validar usuario con organización
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public UserModel getUserByUsernameAndOrganization(RealmModel realm, String username, String acronimo) {
    logger.debugf("getUserByUsernameAndOrganization called with username: %s, organization: %s",
        username, acronimo);

    EntityManager em = getEntityManager();
    try {
      TypedQuery<Usuario> query = em.createQuery(
          "SELECT DISTINCT u FROM UsuarioExterno u JOIN u.usuarioRoles ur JOIN ur.organizacion o WHERE u.username = :username AND o.acronimo = :acronimo AND u.estado = 'ACT' AND ur.estado = 'ACT'", Usuario.class);

      query.setParameter("username", username);
      query.setParameter("acronimo", acronimo);

      Usuario usuario = query.getResultStream().findFirst().orElse(null);

      if (usuario != null) {
        logger.debugf("Usuario '%s' encontrado en organización '%s'", username, acronimo);
        ConsejoJudicaturaAdapter adapter = new ConsejoJudicaturaAdapter(session, realm, model, usuario);
        adapter.setOrganizacion(acronimo);
        return adapter;
      } else {
        logger.debugf("Usuario '%s' no encontrado en organización '%s'", username, acronimo);
      }
    } catch (Exception e) {
      logger.errorv(e, "Error al buscar usuario {0} en organización {1}", username, acronimo);
    } finally {
      // No cerrar aquí si lo vas a usar en otros métodos durante la misma sesión
      // Se cerrará en el método close() del provider
      if(em != null && em.isOpen()) {
        em.close();
      }
    }

    return null;
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public UserModel getUserByEmail(String email, RealmModel realm) {
    logger.debugf("getUserByEmail called with email: %s", email);

    EntityManager em = getEntityManager();
    try {
      TypedQuery<Usuario> query = em.createQuery(
          "SELECT u FROM UsuarioExterno u JOIN u.persona p WHERE p.email = :email AND u.estado = 'ACT'", Usuario.class);

      query.setParameter("email", email);
      Usuario usuario = query.getResultStream().findFirst().orElse(null);

      if (usuario != null) {
        logger.debugf("Usuario con email '%s' encontrado", email);
        return new ConsejoJudicaturaAdapter(session, realm, model, usuario);
      } else {
        logger.debugf("Usuario con email '%s' no encontrado", email);
      }
    } catch (Exception e) {
      logger.errorv(e, "Error al buscar usuario por email: {0}", email);
    } finally {
      // No cerrar aquí
      if(em != null && em.isOpen()) {
        em.close();
      }
    }

    return null;
  }

  // Implementar métodos de búsqueda
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
    try {
      TypedQuery<Usuario> query = getEntityManager().createQuery(
          "SELECT u FROM Usuario u WHERE " +
              "(u.username LIKE :search OR " +
              "u.persona.nombres LIKE :search OR " +
              "u.persona.apellidos LIKE :search) " +
              "AND u.estado = 'ACT' AND u.tipo = 'EXT'", Usuario.class);

      query.setParameter("search", "%" + search + "%");
      if (firstResult != null) query.setFirstResult(firstResult);
      if (maxResults != null) query.setMaxResults(maxResults);

      return query.getResultStream()
          .map(usuario -> new ConsejoJudicaturaAdapter(session, realm, model, usuario));
    } catch (Exception e) {
      logger.error("Error searching users", e);
      return Stream.empty();
    }
  }

  @Override
  public List<UserModel> searchForUser(Map<String, String> params, RealmModel realmModel) {
    return searchForUser(params, realmModel, 0, Integer.MAX_VALUE);
  }

  @Override
  public List<UserModel> searchForUser(Map<String, String> params, RealmModel realmModel, int firstResult, int maxResults) {
    try {
      StringBuilder jpql = new StringBuilder("SELECT u FROM Usuario u WHERE u.estado = 'ACT' AND u.tipo = 'EXT'");

      if (params.containsKey("username")) {
        jpql.append(" AND u.username LIKE :username");
      }
      if (params.containsKey("email")) {
        jpql.append(" AND u.persona.email LIKE :email");
      }
      if (params.containsKey("firstName")) {
        jpql.append(" AND u.persona.nombres LIKE :firstName");
      }
      if (params.containsKey("lastName")) {
        jpql.append(" AND u.persona.apellidos LIKE :lastName");
      }

      jpql.append(" ORDER BY u.username");

      TypedQuery<Usuario> query = getEntityManager().createQuery(jpql.toString(), Usuario.class);

      params.forEach((key, value) -> {
        if (List.of("username", "email", "firstName", "lastName").contains(key)) {
          query.setParameter(key, "%" + value + "%");
        }
      });

      query.setFirstResult(firstResult);
      query.setMaxResults(maxResults);

      return query.getResultStream()
          .map(usuario -> new ConsejoJudicaturaAdapter(session, realmModel, model, usuario))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error searching users with params", e);
      return List.of();
    }
  }

  @Override
  public List<UserModel> getGroupMembers(RealmModel realmModel, GroupModel groupModel) {
    return List.of();
  }

  @Override
  public List<UserModel> getGroupMembers(RealmModel realmModel, GroupModel groupModel, int i,
      int i1) {
    return List.of();
  }

  @Override
  public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
    return Stream.empty();
  }

  @Override
  public List<UserModel> searchForUserByUserAttribute(String s, String s1, RealmModel realmModel) {
    return List.of();
  }

  @Override
  public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
    return Stream.empty();
  }

  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public int getUsersCount(RealmModel realm) {
    try {
      TypedQuery<Long> query = getEntityManager().createQuery(
          "SELECT COUNT(u) FROM UsuarioExterno u WHERE u.estado = 'ACT'", Long.class);
      return query.getSingleResult().intValue();
    } catch (Exception e) {
      logger.error("Error counting users", e);
      return 0;
    }
  }


  @Override
  public List<UserModel> getUsers(RealmModel realmModel) {
    return getUsers(realmModel, 0, Integer.MAX_VALUE);
  }

  @Override
  public List<UserModel> getUsers(RealmModel realmModel, int firstResult, int maxResults) {
    try {
      TypedQuery<Usuario> query = getEntityManager().createQuery(
          "SELECT u FROM Usuario u WHERE u.estado = 'ACT' ORDER BY u.username", Usuario.class);
      query.setFirstResult(firstResult);
      query.setMaxResults(maxResults);

      return query.getResultStream()
          .map(usuario -> new ConsejoJudicaturaAdapter(session, realmModel, model, usuario))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error getting users", e);
      return List.of();
    }
  }

  @Override
  public List<UserModel> searchForUser(String search, RealmModel realmModel) {
    return searchForUser(search, realmModel, 0, Integer.MAX_VALUE);
  }

  @Override
  public List<UserModel> searchForUser(String search, RealmModel realmModel, int firstResult, int maxResults) {
    try {
      TypedQuery<Usuario> query = getEntityManager().createQuery(
          "SELECT u FROM Usuario u WHERE " +
              "(u.username LIKE :search OR " +
              "u.persona.nombres LIKE :search OR " +
              "u.persona.apellidos LIKE :search) " +
              "AND u.estado = 'ACT' AND u.tipo = 'EXT' ORDER BY u.username", Usuario.class);

      query.setParameter("search", "%" + search + "%");
      query.setFirstResult(firstResult);
      query.setMaxResults(maxResults);

      return query.getResultStream()
          .map(usuario -> new ConsejoJudicaturaAdapter(session, realmModel, model, usuario))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error searching users", e);
      return List.of();
    }
  }

}