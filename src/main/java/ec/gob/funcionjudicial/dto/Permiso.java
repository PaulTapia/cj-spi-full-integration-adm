/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase Permiso 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.dto;

import ec.gob.funcionjudicial.enums.NivelAcceso;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

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
@Setter
@Getter
public class Permiso implements Serializable {

  private static final long serialVersionUID = 1L;
  private List<NivelAcceso> nivelesAcceso;
  private String nombre;
  private String url;

}