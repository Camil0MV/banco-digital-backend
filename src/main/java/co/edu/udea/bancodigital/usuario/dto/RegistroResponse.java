package co.edu.udea.bancodigital.usuario.dto;

import co.edu.udea.bancodigital.models.enums.Rol;
import co.edu.udea.bancodigital.models.enums.TipoDocumento;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistroResponse {

    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String correo;
    private Rol rol;
}
