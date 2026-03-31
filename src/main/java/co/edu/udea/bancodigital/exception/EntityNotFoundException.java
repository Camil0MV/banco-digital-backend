package co.edu.udea.bancodigital.exception;

/**
 * Excepción personalizada para cuando una entidad no es encontrada.
 * Se lanza cuando se intenta acceder a un recurso que no existe.
 */
public class EntityNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EntityNotFoundException(String message) {
		super(message);
	}

	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
