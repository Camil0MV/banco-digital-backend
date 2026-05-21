package co.edu.udea.bancodigital.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoBancarioDTO {
    private String nombreCompleto;
    private LocalDate fechaConsulta;
    private String nombreProducto;
    private String numeroCuenta;
    private LocalDateTime fechaInicio;
    private BigDecimal balance;
    private String estadoCuenta;
}
