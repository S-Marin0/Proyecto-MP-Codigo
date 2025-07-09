package modelo.excepciones;

public class AplicacionException extends RuntimeException {
    public AplicacionException(String message) {
        super(message);
    }

    public AplicacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
