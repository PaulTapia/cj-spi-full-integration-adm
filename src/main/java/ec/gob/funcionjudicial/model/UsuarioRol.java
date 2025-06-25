/**
 * <p> Proyecto cj-spi-user-storage-adm.
 * <p> Clase UsuarioRol 19/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * -- AQUI AÑADIR LA DESCRIPCION DE LA CLASE --.
 *
 * <p>Historial de cambios:
 *
 * <ul>
 *   <li>1.0.0 - Descripción del cambio inicial - Danny.Tapia - 19/6/2025
 *       <!-- Añadir nuevas entradas de cambios aquí -->
 * </ul>
 *
 * @author Danny.Tapia
 * @version 1.0.0 $
 * @since 19/6/2025
 */
@Entity
@Table(name = "UsuarioRol", catalog = "PORTAL_APLICATIVOS_CJ", schema = "ADM")
@Setter
@Getter
public class UsuarioRol implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "idUsuario")
  private Usuario usuario;

  @ManyToOne
  @JoinColumn(name = "idOrganizacion")
  private Organizacion organizacion;

  @ManyToOne
  @JoinColumn(name = "idRol")
  private Rol rol;

  @Column(name = "fechaInicio", columnDefinition = "date default getdate()")
  private LocalDate fechaInicio;

  private LocalDateTime fechaFin;
  private String estado;

}