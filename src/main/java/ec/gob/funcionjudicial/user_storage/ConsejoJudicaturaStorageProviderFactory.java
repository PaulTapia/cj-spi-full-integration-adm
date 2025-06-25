/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaStorageProviderFactory 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.user_storage;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

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
public class ConsejoJudicaturaStorageProviderFactory implements
    UserStorageProviderFactory<ConsejoJudicaturaStorageProvider> {

  // ID único para nuestro proveedor.
  public static final String PROVIDER_ID = "cj-storage-adm";

  @Inject
  ConsejoJudicaturaStorageProvider storageProvider;

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getHelpText() {
    return "ADM Aplicativos User Storage Provider";
  }

  @Override
  public ConsejoJudicaturaStorageProvider create(KeycloakSession session, ComponentModel model) {
    storageProvider.setSession(session);
    storageProvider.setModel(model);
    return storageProvider;
  }

}