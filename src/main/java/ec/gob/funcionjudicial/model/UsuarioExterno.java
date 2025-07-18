/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase UsuarioExterno 15/7/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.model;

import java.io.Serializable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * -- AQUI AÑADIR LA DESCRIPCION DE LA CLASE --.
 *
 * <p>Historial de cambios:
 *
 * <ul>
 *   <li>1.0.0 - Descripción del cambio inicial - Danny.Tapia - 15/7/2025
 *       <!-- Añadir nuevas entradas de cambios aquí -->
 * </ul>
 *
 * @author Danny.Tapia
 * @version 1.0.0 $
 * @since 15/7/2025
 */
@Entity
@DiscriminatorValue("EXT")
public class UsuarioExterno extends Usuario implements Serializable {
  private static final long serialVersionUID = 1L;

}