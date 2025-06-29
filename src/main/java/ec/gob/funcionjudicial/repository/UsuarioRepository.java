/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase UsuarioRepository 26/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.repository;

import ec.gob.funcionjudicial.model.HistoricoAccesos;
import ec.gob.funcionjudicial.model.Usuario;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import org.jboss.logging.Logger;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

/**
 * -- AQUI AÑADIR LA DESCRIPCION DE LA CLASE --.
 *
 * <p>Historial de cambios:
 *
 * <ul>
 *   <li>1.0.0 - Descripción del cambio inicial - Danny.Tapia - 26/6/2025
 *       <!-- Añadir nuevas entradas de cambios aquí -->
 * </ul>
 *
 * @author Danny.Tapia
 * @version 1.0.0 $
 * @since 26/6/2025
 */
@ApplicationScoped
public class UsuarioRepository {

  private static final Logger logger = Logger.getLogger(UsuarioRepository.class);

  private EntityManager getEntityManager(KeycloakSession session) {
    return session.getProvider(JpaConnectionProvider.class, "adm").getEntityManager();
  }

  public Optional<Usuario> findByUsername(KeycloakSession session, String username) {
    try {
      TypedQuery<Usuario> query = getEntityManager(session).createQuery(
          "SELECT u FROM Usuario u WHERE u.username = :username AND u.estado = 'ACT' AND u.tipo = 'EXT'",
          Usuario.class);
      query.setParameter("username", username);
      return query.getResultStream().findFirst();
    } catch (Exception e) {
      logger.error("Error finding user by username: " + username, e);
      return Optional.empty();
    }
  }

  public Optional<Usuario> findByUsernameAndOrganization(KeycloakSession session, String username, String acronimo) {
    try {
      TypedQuery<Usuario> query = getEntityManager(session).createQuery(
          "SELECT DISTINCT u FROM Usuario u " +
              "JOIN u.usuarioRoles ur " +
              "JOIN ur.organizacion o " +
              "WHERE u.username = :username AND o.acronimo = :acronimo " +
              "AND u.estado = 'ACT' AND u.tipo = 'EXT' AND ur.estado = 'ACT'", Usuario.class);
      query.setParameter("username", username);
      query.setParameter("acronimo", acronimo);
      return query.getResultStream().findFirst();
    } catch (Exception e) {
      logger.error("Error finding user by username and organization: " + username + ", " + acronimo, e);
      return Optional.empty();
    }
  }

  public Optional<Usuario> findByEmail(KeycloakSession session, String email) {
    try {
      TypedQuery<Usuario> query = getEntityManager(session).createQuery(
          "SELECT u FROM Usuario u " +
              "JOIN u.persona p " +
              "WHERE p.email = :email AND u.estado = 'ACT' AND u.tipo = 'EXT'", Usuario.class);
      query.setParameter("email", email);
      return query.getResultStream().findFirst();
    } catch (Exception e) {
      logger.error("Error finding user by email: " + email, e);
      return Optional.empty();
    }
  }

  public List<Usuario> searchUsers(KeycloakSession session, String search, Integer firstResult, Integer maxResults) {
    try {
      TypedQuery<Usuario> query = getEntityManager(session).createQuery(
          "SELECT u FROM Usuario u WHERE " +
              "(u.username LIKE :search OR " +
              "u.persona.nombres LIKE :search OR " +
              "u.persona.apellidos LIKE :search) " +
              "AND u.estado = 'ACT' AND u.tipo = 'EXT' ORDER BY u.username", Usuario.class);

      query.setParameter("search", "%" + search + "%");
      if (firstResult != null) query.setFirstResult(firstResult);
      if (maxResults != null) query.setMaxResults(maxResults);

      return query.getResultList();
    } catch (Exception e) {
      logger.error("Error searching users with: " + search, e);
      return List.of();
    }
  }

  public List<Usuario> getUsers(KeycloakSession session, int firstResult, int maxResults) {
    try {
      TypedQuery<Usuario> query = getEntityManager(session).createQuery(
          "SELECT u FROM Usuario u WHERE u.estado = 'ACT' AND u.tipo = 'EXT' ORDER BY u.username", Usuario.class);
      query.setFirstResult(firstResult);
      query.setMaxResults(maxResults);
      return query.getResultList();
    } catch (Exception e) {
      logger.error("Error getting users", e);
      return List.of();
    }
  }

  public int getUsersCount(KeycloakSession session) {
    try {
      TypedQuery<Long> query = getEntityManager(session).createQuery(
          "SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'ACT' AND u.tipo = 'EXT'", Long.class);
      return query.getSingleResult().intValue();
    } catch (Exception e) {
      logger.error("Error counting users", e);
      return 0;
    }
  }
}