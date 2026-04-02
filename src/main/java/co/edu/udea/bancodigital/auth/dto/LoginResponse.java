package co.edu.udea.bancodigital.auth.dto;

import co.edu.udea.bancodigital.models.enums.Rol;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String tipo;
    private String nombre;
    private String correo;
    private Rol rol;
}
