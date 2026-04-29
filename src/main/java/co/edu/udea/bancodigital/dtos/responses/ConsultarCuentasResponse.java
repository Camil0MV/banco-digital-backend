package co.edu.udea.bancodigital.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsultarCuentasResponse {

    private int totalCuentas;
    private List<DetalleCuenta> cuentas;

    @Getter
    @Builder
    public static class DetalleCuenta {
        private UUID idCuenta;
        private Integer idTipoCuenta;
        private String tipoCuenta;
        private Integer idEstadoCuenta;
        private String estadoCuenta;
        private BigDecimal saldo;
        private LocalDateTime createdAt;
    }
}
