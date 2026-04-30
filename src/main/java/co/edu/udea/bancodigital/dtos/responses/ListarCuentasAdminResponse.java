package co.edu.udea.bancodigital.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListarCuentasAdminResponse {
    private final UUID idCuenta;
    private final String tipoDocumentoDueno;
    private final String numeroDocumentoDueno;
    private final String nombreCompletoDueno;
    private final String tipoCuenta;
    private final String estadoCuenta;
    private final BigDecimal saldo;
    private final LocalDateTime createdAt;
}
