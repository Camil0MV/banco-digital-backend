package co.edu.udea.bancodigital.dtos.responses;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListarClientesResponse {
	private final Integer idTipoDocumento;
	private final String tipoDocumento;
	private final String numeroDocumento;
	private final String nombre;
	private final String primerApellido;
	private final String segundoApellido;
	private final String correo;
	private final String telefono;
	private final String direccion;
	private final String rol;
	private final LocalDateTime createdAt;
}
