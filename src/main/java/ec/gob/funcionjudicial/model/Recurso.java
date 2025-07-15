/**
 * <p> Proyecto cj-spi-user-storage-adm.
 * <p> Clase Recurso 19/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.model;

import ec.gob.funcionjudicial.dto.Opcion;
import ec.gob.funcionjudicial.dto.Permiso;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
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
@Table(name = "Recurso", catalog = "PORTAL_APLICATIVOS_CJ", schema = "ADM")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
@Setter
@Getter
public abstract class Recurso implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(insertable = false, updatable = false)
  private String tipo;
  private String nombre;
  private String url;

  @ManyToOne
  @JoinColumn(name = "idRecursoPadre")
  private Recurso padre;

  @Column(name = "menu", columnDefinition = "bit default 1")
  private Boolean menu;

  @ManyToMany(mappedBy = "recursos")
  private Set<Rol> roles = new HashSet<>();

  public Permiso toPermiso() {
    Permiso permiso = new Permiso();
    permiso.setNombre(this.nombre);
    permiso.setUrl(this.url);
    permiso.setNivelesAcceso(new ArrayList<>());
    return permiso;
  }

  public Opcion toOpcion(List<Long> allowedOpciones) {
    Opcion opcion = new Opcion();
    opcion.setCodigo(this.id);
    opcion.setNombre(this.nombre);
    opcion.setUrl(this.url);
    opcion.setSubOpciones(new ArrayList<>());
    return opcion;
  }

}