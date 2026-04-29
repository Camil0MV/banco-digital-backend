package co.edu.udea.bancodigital.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultarSaldoResponse {

    private UUID idCuenta;
    private BigDecimal saldo;
    private LocalDateTime consultedAt;
}
