/**
 * <p> Proyecto cj-spi-user-storage-adm.
 * <p> Clase HistoricoAccesos 19/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.model;

import java.io.Serializable;
import java.time.LocalDateTime;
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
@Table(name = "HistoricoAccesos", catalog = "PORTAL_APLICATIVOS_CJ", schema = "ADM")
@Setter
@Getter
public class HistoricoAccesos implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "idUsuario")
  private Usuario usuario;

  private String estado;
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaFin;
  private String usuarioRegistra;
  private String usuarioActualiza;
  private String username;
  private String ip;

}