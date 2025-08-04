package ec.gob.funcionjudicial.exception;

public class ConsejoJudicaturaException extends RuntimeException {
    public ConsejoJudicaturaException(String message) {
        super(message);
    }

    public ConsejoJudicaturaException(String message, Throwable cause) {
        super(message, cause);
    }
}
