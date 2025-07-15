/**
 * <p> Proyecto cj-spi-user-storage-adm.
 * <p> Clase Rol 19/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "Rol", catalog = "PORTAL_APLICATIVOS_CJ", schema = "ADM")
@Setter
@Getter
public class Rol implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  @ManyToOne
  @JoinColumn(name = "idRolPadre")
  private Rol padre;

  @ManyToOne
  @JoinColumn(name = "idAplicativo")
  private Aplicativo aplicativo;

  @ManyToMany
  @JoinTable(
      catalog = "PORTAL_APLICATIVOS_CJ",
      name = "RolRecurso",
      schema = "ADM",
      joinColumns = @JoinColumn(name = "idRol"),
      inverseJoinColumns = @JoinColumn(name = "idRecurso")
  )
  private Set<Recurso> recursos = new HashSet<>();

  public ec.gob.funcionjudicial.dto.Rol toIRol() {
    ec.gob.funcionjudicial.dto.Rol rolDto = new ec.gob.funcionjudicial.dto.Rol();
    rolDto.setCodigo(this.id);
    rolDto.setNombre(this.nombre);
    if (this.aplicativo != null) {
      rolDto.setAplicacion(this.aplicativo.getNombre());
    }
    return rolDto;
  }

}