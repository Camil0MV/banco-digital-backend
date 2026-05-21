package co.edu.udea.bancodigital.dtos.responses;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditoriaTransaccionesResponse {

    private List<AuditoriaTransaccionResponse> transacciones;
    private long total;
    private int pagina;
    private int tamanoPagina;
    private int totalPaginas;
    private String mensaje;
    private boolean tieneDatos;
}
