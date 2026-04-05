package co.edu.udea.bancodigital.dtos.responses;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String tipo;
    private String nombre;
    private String correo;
    private Integer idRol;
    private String rol;
}
