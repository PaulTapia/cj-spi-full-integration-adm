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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import org.jboss.logging.Logger;

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

  @Inject
  @PersistenceUnit(unitName = "adm")
  EntityManager em;

  // El código de los métodos findByUsername, findByEmail, etc.
  // es IDÉNTICO al de la respuesta anterior.
  // Solo cambian los imports.


  public Usuario findByUsername(String username) {
    try {
      TypedQuery<Usuario> query = em.createQuery(
          "SELECT u FROM Usuario u WHERE u.username = :username AND u.estado = 'ACT'",
          Usuario.class);
      query.setParameter("username", username);
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public Usuario findByEmail(String email) {
    try {
      TypedQuery<Usuario> query = em.createQuery(
          "SELECT u FROM Usuario u JOIN FETCH u.persona p WHERE p.email = :email AND u.estado = 'ACT'",
          Usuario.class);
      query.setParameter("email", email);
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public Usuario findByUsernameAndOrganization(String username, String acronimo) {
    try {
      // --- CORRECCIÓN CRÍTICA DE LA CONSULTA ---
      // Ahora la consulta empieza desde UsuarioRol y devuelve el Usuario asociado.
      // Esto es mucho más eficiente y correcto según tu DDL.
      TypedQuery<Usuario> query = em.createQuery(
          "SELECT ur.usuario FROM UsuarioRol ur " +
              "WHERE ur.usuario.username = :username AND ur.organizacion.acronimo = :acronimo " +
              "AND ur.usuario.estado = 'ACT' AND ur.estado = 'ACT'", Usuario.class);

      query.setParameter("username", username);
      query.setParameter("acronimo", acronimo);

      // Usamos getResultList() porque un usuario podría tener el mismo rol en la misma organización
      // más de una vez (aunque es raro). Tomamos el primer resultado.
      List<Usuario> results = query.getResultList();
      return results.isEmpty() ? null : results.get(0);

    } catch (NoResultException e) {
      return null;
    }
  }

  public void registrarAcceso(Usuario usuario, boolean exitoso, String acronimo) {
    try {
      HistoricoAccesos acceso = new HistoricoAccesos();
      acceso.setUsuario(usuario);
      acceso.setUsername(usuario.getUsername());
      acceso.setEstado(exitoso ? "OK" : "NO");
      acceso.setFechaInicio(java.time.LocalDateTime.now());
      acceso.setUsuarioRegistra("KEYCLOAK");
      if (acronimo != null) {
        acceso.setIp(acronimo);
      }
      em.persist(acceso);
      logger.infof("Acceso registrado para el usuario: %s, Exitoso: %s", usuario.getUsername(),
          exitoso);
    } catch (Exception e) {
      logger.errorv(e, "Error al registrar acceso para el usuario {0}", usuario.getUsername());
    }
  }

}