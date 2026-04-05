package co.edu.udea.bancodigital.dtos.responses;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistroResponse {

    private Integer idTipoDoc;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String correo;
    private Integer idRol;
    private String rol;
}
