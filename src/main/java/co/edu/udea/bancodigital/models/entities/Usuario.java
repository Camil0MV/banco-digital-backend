package co.edu.udea.bancodigital.models.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.edu.udea.bancodigital.models.enums.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad Usuario del banco digital.
 * Contiene información de los usuarios del sistema.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends AuditableEntity {

	@EmbeddedId
	private UsuarioId id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rol rol;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private String primerApellido;

	@Column(nullable = false)
	private String segundoApellido;

	@Column(nullable = false)
	private String direccion;

	@Column(nullable = false)
	private String telefono;

	@Column(nullable = false, unique = true)
	private String correo;

	@Column(nullable = false)
	private String contrasena;

	@Column(nullable = false)
	private LocalDateTime fechaRegistro;

	@OneToMany(mappedBy = "dueno")
	@Builder.Default
	private List<Cuenta> cuentas = new ArrayList<>();
}
