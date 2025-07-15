package ec.gob.funcionjudicial.model;

import java.io.Serializable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("MOD")
public class Modulo extends Recurso implements Serializable {

	private static final long serialVersionUID = 1L;

}
