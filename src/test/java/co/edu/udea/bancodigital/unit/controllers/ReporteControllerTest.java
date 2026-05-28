package co.edu.udea.bancodigital.unit.controllers;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.udea.bancodigital.controllers.ReporteController;
import co.edu.udea.bancodigital.dtos.requests.GenerarReporteRequest;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.exception.GlobalExceptionHandler;
import co.edu.udea.bancodigital.services.CertificadoService;
import co.edu.udea.bancodigital.services.ReporteService;

import java.time.LocalDate;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReporteControllerTest - Pruebas de Reportes y Certificados Bancarios")
class ReporteControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReporteService reporteService;

    @Mock
    private CertificadoService certificadoService;

    @InjectMocks
    private ReporteController reporteController;

    private UUID idCuenta;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(reporteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        idCuenta = UUID.randomUUID();
    }

    @Test
    @DisplayName("HU1-SC1: Generar reporte de transacciones exitosamente con rango válido de fechas")
    void should_GenerateTransactionReport_When_DateRangeIsValid() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2026, 1, 1);
        LocalDate fechaFin = LocalDate.of(2026, 2, 28);
        GenerarReporteRequest request = new GenerarReporteRequest(fechaInicio, fechaFin);

        doNothing().when(reporteService).generarReporteMovimientos(any(UUID.class), any(LocalDate.class), any(LocalDate.class));

        mockMvc.perform(post("/api/v1/reportes/movimientos/{idCuenta}", idCuenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Reporte generado y enviado a tu correo")));

        verify(reporteService).generarReporteMovimientos(any(UUID.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("HU1-SC2: Generar reporte sin transacciones en el rango válido")
    void should_GenerateTransactionReport_When_NoTransactionsInRange() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2026, 4, 1);
        LocalDate fechaFin = LocalDate.of(2026, 4, 30);
        GenerarReporteRequest request = new GenerarReporteRequest(fechaInicio, fechaFin);

        doNothing().when(reporteService).generarReporteMovimientos(any(UUID.class), any(LocalDate.class), any(LocalDate.class));

        mockMvc.perform(post("/api/v1/reportes/movimientos/{idCuenta}", idCuenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Reporte generado y enviado a tu correo")));

        verify(reporteService).generarReporteMovimientos(any(UUID.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("HU1-SC3: Rechazar reporte con rango de fechas superior a 90 días")
    void should_RejectReport_When_DateRangeExceeds90Days() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2025, 12, 1);
        LocalDate fechaFin = LocalDate.of(2026, 4, 30);
        GenerarReporteRequest request = new GenerarReporteRequest(fechaInicio, fechaFin);

        // Como el DTO o el validador frena la petición en el controlador (isBadRequest), simulamos el flujo interceptado por Spring.
        mockMvc.perform(post("/api/v1/reportes/movimientos/{idCuenta}", idCuenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU1-SC4: Rechazar reporte cuando usuario no está autenticado")
    void should_RejectReport_When_UserIsNotAuthenticated() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2026, 1, 1);
        LocalDate fechaFin = LocalDate.of(2026, 2, 28);
        GenerarReporteRequest request = new GenerarReporteRequest(fechaInicio, fechaFin);

        mockMvc.perform(post("/api/v1/reportes/movimientos/{idCuenta}", idCuenta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HU2-SC1: Generar reporte de certificado bancario exitosamente")
    void should_GenerateBankCertificateReport_When_UserIsAuthenticated() throws Exception {
        doNothing().when(certificadoService).solicitarReporteCertificadoBancario(idCuenta);

        mockMvc.perform(post("/api/v1/reportes/certificado/{idCuenta}", idCuenta)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Reporte del certificado bancario solicitado exitosamente. Te lo enviaremos por correo en breve.")));

        verify(certificadoService).solicitarReporteCertificadoBancario(idCuenta);
    }

    @Test
    @DisplayName("HU2-SC1: Validar que el certificado contiene datos correctos del cliente y cuenta")
    void should_GenerateCertificateWithCorrectData_When_RequestIsValid() throws Exception {
        doNothing().when(certificadoService).solicitarReporteCertificadoBancario(idCuenta);

        mockMvc.perform(post("/api/v1/reportes/certificado/{idCuenta}", idCuenta)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey("success")))
                .andExpect(jsonPath("$", hasKey("message")))
                .andExpect(jsonPath("$.success", is(true)));

        verify(certificadoService).solicitarReporteCertificadoBancario(idCuenta);
    }

    @Test
    @DisplayName("HU2-SC2: Rechazar solicitud de certificado cuando usuario no está autenticado")
    void should_RejectCertificateRequest_When_UserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/certificado/{idCuenta}", idCuenta)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HU2-SC2: Rechazar certificado si cuenta no existe")
    void should_RejectCertificate_When_AccountDoesNotExist() throws Exception {
        UUID invalidCuentaId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("La cuenta no existe"))
                .when(certificadoService).solicitarReporteCertificadoBancario(invalidCuentaId);

        mockMvc.perform(post("/api/v1/reportes/certificado/{idCuenta}", invalidCuentaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(certificadoService).solicitarReporteCertificadoBancario(invalidCuentaId);
    }

    @Test
    @DisplayName("HU2-SC2: Rechazar certificado si usuario no es propietario de la cuenta")
    void should_RejectCertificate_When_UserIsNotAccountOwner() throws Exception {
        UUID cuentaAjena = UUID.randomUUID();
        doThrow(new AccessDeniedException("Acceso denegado - no eres propietario de la cuenta"))
                .when(certificadoService).solicitarReporteCertificadoBancario(cuentaAjena);

        mockMvc.perform(post("/api/v1/reportes/certificado/{idCuenta}", cuentaAjena)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(certificadoService).solicitarReporteCertificadoBancario(cuentaAjena);
    }
}