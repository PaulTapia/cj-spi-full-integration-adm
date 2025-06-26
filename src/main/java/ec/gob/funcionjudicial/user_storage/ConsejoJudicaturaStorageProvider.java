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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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
public class ConsejoJudicaturaStorageProvider implements
    UserStorageProvider,
    UserLookupProvider,
    CredentialInputValidator {

  private static final Logger logger = Logger.getLogger(ConsejoJudicaturaStorageProvider.class);

  @Setter
  @Getter
  private KeycloakSession session;
  @Setter
  @Getter
  private ComponentModel model;
  @Inject
  EntityManager em;

  private final Map<String, UserModel> userCache = new ConcurrentHashMap<>();


  private Connection getConnection() {
    try {
      String url = "jdbc:sqlserver://10.1.27.24:1438;databaseName=PORTAL_APLICATIVOS_CJ;instanceName=DESA02;encrypt=false;trustServerCertificate=false;loginTimeout=30";
      return DriverManager.getConnection(url, "USR_ADM_DES_ALL", "uadacjt2012");
    } catch (Exception e) {
      logger.error("Error creating connection", e);
      return null;
    }
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
  @Transactional
  public boolean isValid(RealmModel realmModel, UserModel userModel,
      CredentialInput credentialInput) {
    if (!supportsCredentialType(credentialInput.getType()) || !(credentialInput instanceof UserCredentialModel)) {
      return false;
    }

    UserCredentialModel cred = (UserCredentialModel) credentialInput;
    String username = userModel.getUsername();
    String acronimo = getOrganizacionFromContext();

    try (Connection conn = getConnection()) {
      if (conn == null) return false;

      Usuario usuario = null;

      if (acronimo != null && !acronimo.isEmpty()) {
        // Validar con organización específica
        String sql = "SELECT DISTINCT u.id, u.username, u.password, u.estado " +
            "FROM ADM.Usuario u " +
            "JOIN ADM.UsuarioRol ur ON ur.idUsuario = u.id " +
            "JOIN ADM.Organizacion o ON o.id = ur.idOrganizacion " +
            "WHERE u.username = ? AND o.acronimo = ? " +
            "AND u.estado = 'ACT' AND ur.estado = 'ACT'";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, acronimo);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          usuario = new Usuario();
          usuario.setId(rs.getLong("id"));
          usuario.setUsername(rs.getString("username"));
          usuario.setPassword(rs.getString("password"));
          usuario.setEstado(rs.getString("estado"));
        }
      } else {
        // Fallback: validar sin organización
        String sql = "SELECT id, username, password, estado FROM ADM.Usuario WHERE username = ? AND estado = 'ACT'";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          usuario = new Usuario();
          usuario.setId(rs.getLong("id"));
          usuario.setUsername(rs.getString("username"));
          usuario.setPassword(rs.getString("password"));
          usuario.setEstado(rs.getString("estado"));
        }
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

  private String getOrganizacionFromContext() {
    try {

        AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
        if (authSession != null) {
          return authSession.getUserSessionNotes().get("organizacion");
        }

    } catch (Exception e) {
      // Log error
    }
    return null;
  }


  @Transactional
  public void registrarAcceso(Usuario usuario, boolean exitoso, String acronimo) {
    try (Connection conn = getConnection()) {
      if (conn == null) return;

      String sql = "INSERT INTO ADM.HistoricoAccesos (idUsuario, estado, fechaInicio, fechaFin, usuarioRegistra, usuarioActualiza, username, ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, usuario.getId());
      stmt.setString(2, exitoso ? "OK" : "NO");
      java.sql.Timestamp now = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
      stmt.setTimestamp(3, now);
      stmt.setTimestamp(4, now);
      stmt.setString(5, "KEYCLOAK");
      stmt.setString(6, "KEYCLOAK");
      stmt.setString(7, usuario.getUsername()); // varchar(50)
      stmt.setString(8, getClientIP()); // varchar(40)

      stmt.executeUpdate();
    } catch (Exception e) {
      logger.error("Error registrando acceso", e);
    }
  }

  private String getClientIP() {
    try {
      return session.getContext().getConnection().getRemoteAddr();
    } catch (Exception e) {
      return "unknown";
    }
  }

  @Override
  public void close() {

  }

  @Override
  @Transactional
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
  @Transactional
  public UserModel getUserByUsername(String s, RealmModel realmModel) {
    // Para compatibilidad con login estándar, buscar sin organización
    try (Connection conn = getConnection()) {
      if (conn == null) return null;

      String sql = "SELECT id, username, password, estado FROM ADM.Usuario WHERE username = ? AND estado = 'ACT'";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, s);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        // Crear objeto Usuario manualmente desde ResultSet
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setEstado(rs.getString("estado"));

        return new ConsejoJudicaturaAdapter(session, realmModel, model, usuario);
      }
    } catch (Exception e) {
      logger.error("Error querying user", e);
    }
    return null;
  }

  // Método específico para validar usuario con organización
  @Transactional
  public UserModel getUserByUsernameAndOrganization(RealmModel realm, String username, String acronimo) {
    try (Connection conn = getConnection()) {
      if (conn == null) return null;

      String sql = "SELECT DISTINCT u.id, u.username, u.password, u.estado " +
          "FROM ADM.Usuario u " +
          "JOIN ADM.UsuarioRol ur ON ur.idUsuario = u.id " +
          "JOIN ADM.Organizacion o ON o.id = ur.idOrganizacion " +
          "WHERE u.username = ? AND o.acronimo = ? " +
          "AND u.estado = 'ACT' AND ur.estado = 'ACT'";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, username);
      stmt.setString(2, acronimo);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setEstado(rs.getString("estado"));

        ConsejoJudicaturaAdapter adapter = new ConsejoJudicaturaAdapter(session, realm, model, usuario);
        adapter.setOrganizacion(acronimo);
        return adapter;
      }
    } catch (Exception e) {
      logger.error("Error querying user by organization", e);
    }
    return null;
  }

  @Override
  public UserModel getUserByEmail(String email, RealmModel realmModel) {
    try (Connection conn = getConnection()) {
      if (conn == null) return null;

      String sql = "SELECT u.id, u.username, u.password, u.estado " +
          "FROM ADM.Usuario u " +
          "JOIN ADM.Persona p ON p.id = u.idPersona " +
          "WHERE p.email = ? AND u.estado = 'ACT'";

      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, email);

      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setEstado(rs.getString("estado"));

        return new ConsejoJudicaturaAdapter(session, realmModel, model, usuario);
      }
    } catch (Exception e) {
      logger.error("Error querying user by email", e);
    }
    return null;
  }

}