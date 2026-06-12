package co.edu.udea.bancodigital.exception;

public class ReporteGenerationException extends RuntimeException {
    public ReporteGenerationException(String message) {
        super(message);
    }

    public ReporteGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
