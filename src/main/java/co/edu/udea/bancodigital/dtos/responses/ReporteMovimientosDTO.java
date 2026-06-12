package co.edu.udea.bancodigital.dtos.responses;

import co.edu.udea.bancodigital.models.entities.Transaccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMovimientosDTO {

    private String nombreCliente;
    private String numeroCuenta;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaConsulta;
    private BigDecimal saldoFinal;
    private List<Transaccion> transacciones;
}
