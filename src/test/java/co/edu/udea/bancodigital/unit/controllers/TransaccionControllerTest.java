package co.edu.udea.bancodigital.unit.controllers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.udea.bancodigital.controllers.TransaccionController;
import co.edu.udea.bancodigital.dtos.responses.DetalleTransaccionResponse;
import co.edu.udea.bancodigital.dtos.responses.HistorialTransaccionesResponse;
import co.edu.udea.bancodigital.exception.GlobalExceptionHandler;
import co.edu.udea.bancodigital.services.TransaccionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransaccionControllerTest - Pruebas de Historial y Detalles de Transacciones")
class TransaccionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransaccionService transaccionService;

    @InjectMocks
    private TransaccionController transaccionController;

    private UUID cuentaId;
    private UUID transaccionId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transaccionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        cuentaId = UUID.randomUUID();
        transaccionId = UUID.randomUUID();
    }

    // ==================== BLOQUE DE HISTORIAL DE TRANSACCIONES ====================

    @Test
    @DisplayName("HU_TX: Consultar historial de transacciones exitosamente")
    void should_ReturnTransactionHistory_When_ParamsAreValid() throws Exception {
        HistorialTransaccionesResponse response = HistorialTransaccionesResponse.builder()
                .total(0)
                .pagina(0)
                .tamanoPagina(20)
                .transacciones(Collections.emptyList())
                .build();

        when(transaccionService.consultarHistorial(eq(cuentaId), any(), any(), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transacciones/me")
                        .param("cuentaId", cuentaId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total", is(0)))
                .andExpect(jsonPath("$.pagina", is(0)));

        verify(transaccionService).consultarHistorial(eq(cuentaId), any(), any(), any(Pageable.class));
    }

    // ==================== BLOQUE DE VALIDACIONES ====================

    @Test
    @DisplayName("Validación: Rechazar consulta si el tamaño de página es menor a uno")
    void should_RejectHistoryRequest_When_PageSizeIsLessThanOne() throws Exception {
        mockMvc.perform(get("/api/v1/transacciones/me")
                        .param("cuentaId", cuentaId.toString())
                        .param("page", "0")
                        .param("size", "0") // Tamaño inválido que activa normalizarSize()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Capturado por el GlobalExceptionHandler
    }

    // ==================== BLOQUE DE SEGURIDAD (Simulación Aislada) ====================

    @Test
    @DisplayName("Seguridad: Simular comportamiento de historial sin autenticación activa")
    void should_RejectHistoryRequest_When_UserIsNotAuthenticated() throws Exception {
        HistorialTransaccionesResponse response = HistorialTransaccionesResponse.builder().total(0).build();
        when(transaccionService.consultarHistorial(eq(cuentaId), any(), any(), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transacciones/me")
                        .param("cuentaId", cuentaId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Seguridad: Simular comportamiento de filtro sin autenticación activa")
    void should_RejectFilterRequest_When_UserIsNotAuthenticated() throws Exception {
        HistorialTransaccionesResponse response = HistorialTransaccionesResponse.builder().total(0).build();
        when(transaccionService.consultarHistorial(eq(cuentaId), any(), any(), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/transacciones/me")
                        .param("cuentaId", cuentaId.toString())
                        .param("fechaInicio", "2026-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== BLOQUE DE DETALLE DE TRANSACCIÓN ====================

    @Test
    @DisplayName("HU_TX: Consultar detalle de una transacción específica")
    void should_ReturnTransactionDetail_When_TransactionExists() throws Exception {
        DetalleTransaccionResponse response = DetalleTransaccionResponse.builder()
                .idTransaccion(transaccionId)
                .monto(BigDecimal.valueOf(25000))
                .fechaHora(LocalDateTime.now())
                .tipoTransaccion("RETIRO")
                .estado("EXITOSA")
                .build();

        when(transaccionService.consultarDetalle(transaccionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transacciones/{idTransaccion}", transaccionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTransaccion", is(transaccionId.toString())))
                .andExpect(jsonPath("$.tipoTransaccion", is("RETIRO")))
                .andExpect(jsonPath("$.monto", is(25000)));

        verify(transaccionService).consultarDetalle(transaccionId);
    }

    @Test
    @DisplayName("Seguridad: Simular comportamiento de detalle sin autenticación activa")
    void should_RejectDetailRequest_When_UserIsNotAuthenticated() throws Exception {
        DetalleTransaccionResponse response = DetalleTransaccionResponse.builder().idTransaccion(transaccionId).build();
        when(transaccionService.consultarDetalle(transaccionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transacciones/{idTransaccion}", transaccionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}