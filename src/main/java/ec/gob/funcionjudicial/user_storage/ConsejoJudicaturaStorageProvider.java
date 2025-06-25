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
import io.quarkus.hibernate.orm.PersistenceUnit;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import lombok.Getter;
import lombok.Setter;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
  @PersistenceUnit("ADM")
  EntityManager em;


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

    // Obtener organización desde el contexto de autenticación
    String acronimo = getOrganizacionFromContext();

    try {
      Usuario usuario;

      if (acronimo != null && !acronimo.isEmpty()) {
        // Validar con organización específica
        usuario = em.createQuery(
                "SELECT DISTINCT u FROM Usuario u " +
                    "JOIN UsuarioRol ur ON ur.usuario.id = u.id " +
                    "JOIN ur.organizacion o " +
                    "WHERE u.username = :username AND o.acronimo = :acronimo " +
                    "AND u.estado = 'A' AND ur.estado = 'A'",
                Usuario.class)
            .setParameter("username", username)
            .setParameter("acronimo", acronimo)
            .getResultStream()
            .findFirst()
            .orElse(null);
      } else {
        // Fallback: validar sin organización
        usuario = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.username = :username AND u.estado = 'A'",
                Usuario.class)
            .setParameter("username", username)
            .getResultStream()
            .findFirst()
            .orElse(null);
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
      // Log error
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
  private void registrarAcceso(Usuario usuario, boolean exitoso, String acronimo) {
    try {
      HistoricoAccesos acceso = new HistoricoAccesos();
      acceso.setUsuario(usuario);
      acceso.setUsername(usuario.getUsername());
      acceso.setEstado(exitoso ? "EXITOSO" : "FALLIDO");
      acceso.setFechaInicio(java.time.LocalDateTime.now());
      acceso.setUsuarioRegistra("KEYCLOAK");
      // Agregar información de organización si está disponible
      if (acronimo != null) {
        acceso.setIp(acronimo); // Usar campo IP temporalmente para organización
      }

      em.persist(acceso);
    } catch (Exception e) {
      // Log error pero no fallar el login
    }
  }

  @Override
  public void close() {

  }

  @Override
  @Transactional
  public UserModel getUserById(String s, RealmModel realmModel) {
    String externalId = StorageId.externalId(s);
    return getUserByUsername(realmModel, externalId);
  }

  @Override
  @Transactional
  public UserModel getUserByUsername(String s, RealmModel realmModel) {
    // Para compatibilidad con login estándar, buscar sin organización
    try {
      Usuario usuario = em.createQuery(
              "SELECT u FROM Usuario u WHERE u.username = :username AND u.estado = 'A'",
              Usuario.class)
          .setParameter("username", s)
          .getResultStream()
          .findFirst()
          .orElse(null);

      if (usuario != null) {
        return new ConsejoJudicaturaAdapter(session, realmModel, model, usuario);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  // Método específico para validar usuario con organización
  @Transactional
  public UserModel getUserByUsernameAndOrganization(RealmModel realm, String username, String acronimo) {
    try {
      Usuario usuario = em.createQuery(
              "SELECT DISTINCT u FROM Usuario u " +
                  "JOIN UsuarioRol ur ON ur.usuario.id = u.id " +
                  "JOIN ur.organizacion o " +
                  "WHERE u.username = :username AND o.acronimo = :acronimo " +
                  "AND u.estado = 'A' AND ur.estado = 'A'",
              Usuario.class)
          .setParameter("username", username)
          .setParameter("acronimo", acronimo)
          .getResultStream()
          .findFirst()
          .orElse(null);

      if (usuario != null) {
        ConsejoJudicaturaAdapter adapter = new ConsejoJudicaturaAdapter(session, realm, model, usuario);
        adapter.setOrganizacion(acronimo); // Guardar organización en el adapter
        return adapter;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  @Transactional
  public UserModel getUserByEmail(String s, RealmModel realmModel) {
    try {
      Usuario usuario = em.createQuery(
              "SELECT u FROM Usuario u JOIN u.persona p WHERE p.email = :email AND u.estado = 'A'",
              Usuario.class)
          .setParameter("email", s)
          .getResultStream()
          .findFirst()
          .orElse(null);

      if (usuario != null) {
        return new ConsejoJudicaturaAdapter(session, realmModel, model, usuario);
      }
    } catch (Exception e) {
      // Log error
    }
    return null;
  }
}