/**
 * <p> Proyecto cj-spi-full-integration-adm.
 * <p> Clase ConsejoJudicaturaAdapter 20/6/2025.
 * <p> Copyright 2025 Consejo de la Judicatura.
 * <p> Todos los derechos reservados.
 */
package ec.gob.funcionjudicial.user_storage;

import ec.gob.funcionjudicial.model.Usuario;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapter;


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
public class ConsejoJudicaturaAdapter extends AbstractUserAdapter {

  private final Usuario usuario;
  private final String keycloakId;
  @Setter
  @Getter
  private String organizacion; // Campo adicional para organización

  public ConsejoJudicaturaAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, Usuario usuario) {
    super(session, realm, model);
    this.usuario = usuario;
    //this.keycloakId = StorageId.keycloakId(model, String.valueOf(usuario.getId()));
    this.keycloakId = StorageId.keycloakId(model, usuario.getUsername());
  }

  @Override
  public String getId() {
    return keycloakId;
  }

  @Override
  public String getUsername() {
    return usuario.getUsername();
  }

  @Override
  public void setUsername(String username) {
    // Read-only
  }

  @Override
  public String getEmail() {
    return usuario.getPersona() != null ? usuario.getPersona().getEmail() : null;
  }

  @Override
  public void setEmail(String email) {
    // Read-only
  }

  @Override
  public String getFirstName() {
    return usuario.getPersona() != null ? usuario.getPersona().getNombres() : null;
  }

  @Override
  public void setFirstName(String firstName) {
    // Read-only
  }

  @Override
  public String getLastName() {
    return usuario.getPersona() != null ? usuario.getPersona().getApellidos() : null;
  }

  @Override
  public void setLastName(String lastName) {
    // Read-only
  }

  @Override
  public boolean isEnabled() {
    return "ACT".equals(usuario.getEstado()) &&
        !Boolean.TRUE.equals(usuario.getBloqueoDefinitivo()) &&
        !Boolean.TRUE.equals(usuario.getBloqueoTemporal());
  }

  @Override
  public void setEnabled(boolean enabled) {
    // Read-only
  }

  @Override
  public boolean isEmailVerified() {
    return getEmail() != null && !getEmail().isEmpty();
  }

  @Override
  public void setEmailVerified(boolean verified) {
    // Read-only
  }

  @Override
  public SubjectCredentialManager credentialManager() {
    return new LegacyUserCredentialManager(session, realm, this);
  }

  // Atributos personalizados
  @Override
  public Stream<String> getAttributeStream(String name) {
    switch (name) {
      case "identificacion":
        return usuario.getPersona() != null && usuario.getPersona().getIdentificacion() != null ?
            Stream.of(usuario.getPersona().getIdentificacion()) : Stream.empty();
      case "celular":
        return usuario.getPersona() != null && usuario.getPersona().getCelular() != null ?
            Stream.of(usuario.getPersona().getCelular()) : Stream.empty();
      case "telefono":
        return usuario.getPersona() != null && usuario.getPersona().getTelefono() != null ?
            Stream.of(usuario.getPersona().getTelefono()) : Stream.empty();
      case "primerIngreso":
        return Stream.of(String.valueOf(usuario.getPrimerIngreso()));
      default:
        return super.getAttributeStream(name);
    }
  }

  @Override
  public Map<String, List<String>> getAttributes() {
    Map<String, List<String>> attrs = super.getAttributes();
    if (usuario.getPersona() != null) {
      if (usuario.getPersona().getIdentificacion() != null) {
        attrs.put("identificacion", List.of(usuario.getPersona().getIdentificacion()));
      }
      if (usuario.getPersona().getCelular() != null) {
        attrs.put("celular", List.of(usuario.getPersona().getCelular()));
      }
    }
    attrs.put("primerIngreso", List.of(String.valueOf(usuario.getPrimerIngreso())));
    return attrs;
  }

}