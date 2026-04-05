package co.edu.udea.bancodigital.exception;

/**
 * Excepción para conflictos por recursos únicos ya existentes.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}