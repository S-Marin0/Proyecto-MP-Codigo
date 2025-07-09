package modelo.excepciones;

public class OperacionInvalidaException extends AplicacionException {
    public OperacionInvalidaException(String message) {
        super(message);
    }
     public OperacionInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
