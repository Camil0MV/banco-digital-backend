package co.edu.udea.bancodigital.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditoriaTransaccionResponse {

    private UUID idTransaccion;
    private String cliente;
    private UUID cuentaOrigen;
    private UUID cuentaDestino;
    private LocalDateTime fechaHora;
    private BigDecimal monto;
    private String estado;
    private String descripcion;
    private String tipoTransaccion;
}
