/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase Rol 23/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * -- AQUI AÑADIR LA DESCRIPCION DE LA CLASE --.
 *
 * <p>Historial de cambios:
 *
 * <ul>
 *   <li>1.0.0 - Descripción del cambio inicial - Danny.Tapia - 23/6/2025
 *       <!-- Añadir nuevas entradas de cambios aquí -->
 * </ul>
 *
 * @author Danny.Tapia
 * @version 1.0.0 $
 * @since 23/6/2025
 */
@Setter
@Getter
public class Rol implements Serializable {

  private static final long serialVersionUID = 1L;
  private Long codigo;
  private String nombre;
  private String aplicacion;

}