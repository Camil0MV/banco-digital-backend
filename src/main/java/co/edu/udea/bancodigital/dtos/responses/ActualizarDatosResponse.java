package co.edu.udea.bancodigital.dtos.responses;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActualizarDatosResponse {

    private Integer idTipoDoc;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String direccion;
    private String telefono;
    private String correo;
    private Integer idRol;
    private String rol;
    private LocalDateTime updatedAt;
}
