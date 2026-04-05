package co.edu.udea.bancodigital.models.pks;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

	@Column(name = "id_tipo_doc", nullable = false)
	private Integer idTipoDoc;

	@Column(name = "numero_documento", nullable = false, length = 50)
	private String numeroDocumento;
}
