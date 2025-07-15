/**
 * <p> Proyecto cj-spi-user-storage-adm.
 * <p> Clase Usuario 19/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.model;

import ec.gob.funcionjudicial.dto.UsuarioFuncionJudicial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@Table(name = "Usuario", catalog = "PORTAL_APLICATIVOS_CJ", schema = "ADM")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
@Setter
@Getter
public class Usuario implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tipo", insertable = false, updatable = false)
  private String tipo;
  private String username;
  private String password;

  @OneToOne
  @JoinColumn(name = "idPersona")
  private Persona persona;

  @Column(name = "fechaRegistro", updatable = false, insertable = false, columnDefinition = "datetime default getdate()")
  private LocalDateTime fechaRegistro;

  private String estado;
  private String usernameSatje;
  private String passwordSatje;
  private Boolean primerIngreso;
  private Boolean bloqueoDefinitivo;
  private Boolean bloqueoTemporal;
  private Boolean caducado;
  private LocalDateTime fechaUltimaClave;

  @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
  private List<UsuarioRol> usuarioRoles;

  public UsuarioFuncionJudicial toUsuarioFuncionJudicial() {
    UsuarioFuncionJudicial user = new UsuarioFuncionJudicial();
    user.setUsuarioPrincipal(this.username);
    if (this.persona != null) {
      user.setNombres(this.persona.getNombres() + " " + this.persona.getApellidos());
    }
    user.setPermisos(new ArrayList<>());
    user.setRoles(new ArrayList<>());
    user.setOpciones(new ArrayList<>());
    return user;
  }

}