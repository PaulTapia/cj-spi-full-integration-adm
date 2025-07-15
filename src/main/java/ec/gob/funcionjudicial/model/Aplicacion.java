package ec.gob.funcionjudicial.model;

import java.io.Serializable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Entity
@DiscriminatorValue("APP")
@Setter
@Getter
public class Aplicacion extends Recurso implements Serializable {

	private static final long serialVersionUID = 1L;

}
