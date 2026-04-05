package co.edu.udea.bancodigital.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActualizarDatosRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ ]+$", message = "El nombre solo puede contener letras y espacios")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ ]+$", message = "El primer apellido solo puede contener letras y espacios")
    @Size(max = 100, message = "El primer apellido no puede superar 100 caracteres")
    private String primerApellido;

    @Pattern(regexp = "^$|^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ ]+$", message = "El segundo apellido solo puede contener letras y espacios")
    @Size(max = 100, message = "El segundo apellido no puede superar 100 caracteres")
    private String segundoApellido;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^3\\d{9}$", message = "El teléfono debe ser celular colombiano: 10 dígitos iniciando en 3")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    private String correo;
}
