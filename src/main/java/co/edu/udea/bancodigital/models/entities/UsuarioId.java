package co.edu.udea.bancodigital.models.entities;

import java.io.Serializable;

import co.edu.udea.bancodigital.models.enums.TipoDocumento;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase clave compuesta para la entidad Usuario.
 * Combina tipo_documento y numero_documento como PK
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	private TipoDocumento tipoDocumento;

	private String numeroDocumento;
}
