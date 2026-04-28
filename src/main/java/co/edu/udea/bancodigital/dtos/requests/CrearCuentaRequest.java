package co.edu.udea.bancodigital.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CrearCuentaRequest {

    @NotNull(message = "El id del tipo de cuenta es obligatorio")
    @Min(value = 1, message = "El id del tipo de cuenta debe ser mayor a cero")
    private Integer idTipoCuenta;
}
