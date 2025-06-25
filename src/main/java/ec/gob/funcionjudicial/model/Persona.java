/**
 * <p> Proyecto cj-spi-user-storage-adm.
 * <p> Clase Persona 19/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.model;

import java.io.Serializable;
import java.time.LocalDate;
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
@Table(name = "Persona", catalog = "PORTAL_APLICATIVOS_CJ", schema = "ADM")
@Setter
@Getter
public class Persona implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String apellidos;
  private String capacidadesEspeciales;
  private String celular;
  private String email;
  private LocalDate fechaNacimiento;
  private String identificacion;
  private String nombres;
  private String telefono;
  private Boolean tieneCapacidadesEspeciales;

  @ManyToOne
  @JoinColumn(name = "idCatalogoEstadoCivil")
  private Catalogo estadoCivil;

  @ManyToOne
  @JoinColumn(name = "idCatalogoGenero")
  private Catalogo genero;

  @ManyToOne
  @JoinColumn(name = "idParroquiaNacimiento")
  private UbicacionGeografica parroquiaNacimiento;

  @ManyToOne
  @JoinColumn(name = "idParroquiaResidencia")
  private UbicacionGeografica parroquiaResidencia;

  @ManyToOne
  @JoinColumn(name = "idCatalogoTipoIdentificacion")
  private Catalogo tipoIdentificacion;

  private String imagenFirma;
  private String nacionalidad;
  private String direccion;

  @ManyToOne
  @JoinColumn(name = "idCatalogoNacionalidad")
  private Catalogo catalogoNacionalidad;

  private String extension;
  private String tipoPersona;

}