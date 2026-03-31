package co.edu.udea.bancodigital.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase estándar para respuestas de error en la API REST.
 * Proporciona información consistente sobre errores ocurridos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

	/**
	 * Código de error específico de la aplicación
	 */
	private String errorCode;

	/**
	 * Mensaje de error principal
	 */
	private String message;

	/**
	 * Detalles adicionales sobre el error
	 */
	private String details;

	/**
	 * ID único para rastrear la solicitud en los logs
	 */
	private String traceId;

	/**
	 * Fecha y hora en que ocurrió el error
	 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime timestamp;
}
