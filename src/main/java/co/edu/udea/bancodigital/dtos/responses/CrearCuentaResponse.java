package co.edu.udea.bancodigital.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CrearCuentaResponse {

    private UUID idCuenta;
    private Integer idTipoCuenta;
    private String tipoCuenta;
    private Integer idEstadoCuenta;
    private String estadoCuenta;
    private BigDecimal saldo;
    private LocalDateTime createdAt;
}
