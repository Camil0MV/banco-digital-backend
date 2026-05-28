package co.edu.udea.bancodigital.unit.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import co.edu.udea.bancodigital.dtos.responses.ReporteMovimientosDTO;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Transaccion;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.catalogs.EstadoCuenta;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoCuenta;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoTransaccion;
import co.edu.udea.bancodigital.models.pks.UsuarioId;
import co.edu.udea.bancodigital.repositories.TransaccionRepository;
import co.edu.udea.bancodigital.services.FinancialReportCommonService;
import co.edu.udea.bancodigital.services.MailService;
import co.edu.udea.bancodigital.services.ReporteGeneratorService;
import co.edu.udea.bancodigital.services.ReporteService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReporteServiceTest - Pruebas de Generación de Reportes de Movimientos")
class ReporteServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ReporteGeneratorService reporteGeneratorService;

    @Mock
    private MailService mailService;

    @Mock
    private FinancialReportCommonService commonService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReporteService reporteService;

    private UUID cuentaId;
    private Usuario usuario;
    private Cuenta cuenta;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private TipoCuenta tipoCuenta;
    private EstadoCuenta estadoCuenta;

    @BeforeEach
    void setUp() {
        // Arrange
        cuentaId = UUID.randomUUID();
        
        usuario = Usuario.builder()
                .id(new UsuarioId(1, "123"))
                .correo("usuario@example.com")
                .nombre("Juan")
                .primerApellido("Pérez")
                .build();

        tipoCuenta = new TipoCuenta();
        tipoCuenta.setId(1);
        tipoCuenta.setNombre("Cuenta de Ahorros Digital");

        estadoCuenta = new EstadoCuenta();
        estadoCuenta.setId(1);
        estadoCuenta.setNombre("ACTIVA");

        cuenta = Cuenta.builder()
                .idCuenta(cuentaId)
                .dueno(usuario)
                .saldo(BigDecimal.valueOf(500000))
                .tipoCuenta(tipoCuenta)
                .estadoCuenta(estadoCuenta)
                .build();

        fechaInicio = LocalDate.of(2026, 1, 1);
        fechaFin = LocalDate.of(2026, 2, 28);

        configureSecurityContext("usuario@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void configureSecurityContext(String email) {
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }

    // ==================== HISTORIA 1: REPORTE DE TRANSACCIONES ====================

    @Test
    @DisplayName("HU1-SC1: Generar reporte de transacciones exitosamente con rango válido")
    void should_GenerateTransactionReport_When_DateRangeIsValid() {
        // Arrange
        TipoTransaccion tipoTransaccion = new TipoTransaccion();
        tipoTransaccion.setId(1);
        tipoTransaccion.setNombre("TRANSFERENCIA");

        Transaccion transaccion = Transaccion.builder()
                .idTransaccion(UUID.randomUUID())
                .cuentaOrigen(cuenta)
                .cuentaDestino(cuenta)
                .tipo(tipoTransaccion)
                .monto(BigDecimal.valueOf(50000))
                .fechaHora(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 100);
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(transaccionRepository.findHistorialByCuentaAndFechaHoraBetween(
                eq(cuentaId), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(transaccion), pageable, 1));

        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDFReporte(any())).thenReturn(new byte[]{1, 2, 3});
        when(reporteGeneratorService.generarCSVReporte(any())).thenReturn(new byte[]{4, 5, 6});

        // Act
        assertDoesNotThrow(() -> reporteService.generarReporteMovimientos(cuentaId, fechaInicio, fechaFin));

        // Assert
        verify(commonService).obtenerUsuarioAutenticado();
        verify(commonService).obtenerCuentaConValidacion(cuentaId);
        verify(commonService).validarPropietarioCuenta(cuenta, usuario);
        verify(transaccionRepository).findHistorialByCuentaAndFechaHoraBetween(
                eq(cuentaId), any(), any(), any());
        verify(reporteGeneratorService).generarPDFReporte(any());
        verify(reporteGeneratorService).generarCSVReporte(any());
        verify(mailService).sendEmailWithAttachments(eq(usuario.getCorreo()), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU1-SC2: Generar reporte sin transacciones en el rango válido")
    void should_GenerateTransactionReport_When_NoTransactionsInRange() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 100);
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(transaccionRepository.findHistorialByCuentaAndFechaHoraBetween(
                eq(cuentaId), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDFReporte(any())).thenReturn(new byte[]{});
        when(reporteGeneratorService.generarCSVReporte(any())).thenReturn(new byte[]{});

        // Act
        assertDoesNotThrow(() -> reporteService.generarReporteMovimientos(cuentaId, fechaInicio, fechaFin));

        // Assert
        verify(mailService).sendEmailWithAttachments(eq(usuario.getCorreo()), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU1-SC3: Rechazar reporte con rango de fechas superior a 90 días")
    void should_RejectReport_When_DateRangeExceeds90Days() {
        // Arrange
        LocalDate fechaInicioInvalida = LocalDate.of(2025, 10, 1);
        LocalDate fechaFinInvalida = LocalDate.of(2026, 4, 30); // Más de 90 días

        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doThrow(new IllegalArgumentException("El rango de fechas no puede ser superior a 90 días"))
                .when(commonService).validarPropietarioCuenta(cuenta, usuario);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> reporteService.generarReporteMovimientos(cuentaId, fechaInicioInvalida, fechaFinInvalida));

        verify(commonService).obtenerUsuarioAutenticado();
        verify(commonService).obtenerCuentaConValidacion(cuentaId);
        verify(mailService, never()).sendEmailWithAttachments(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU1-SC4: Rechazar reporte cuando usuario no está autenticado")
    void should_RejectReport_When_UserIsNotAuthenticated() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado())
                .thenThrow(new EntityNotFoundException("Usuario autenticado no encontrado"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> reporteService.generarReporteMovimientos(cuentaId, fechaInicio, fechaFin));

        verify(commonService).obtenerUsuarioAutenticado();
        verify(mailService, never()).sendEmailWithAttachments(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU1-SC4: Rechazar reporte si usuario no es propietario de la cuenta")
    void should_RejectReport_When_UserIsNotAccountOwner() {
        // Arrange
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doThrow(new AccessDeniedException("No tienes permisos para acceder a esta cuenta"))
                .when(commonService).validarPropietarioCuenta(cuenta, usuario);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> reporteService.generarReporteMovimientos(cuentaId, fechaInicio, fechaFin));

        verify(mailService, never()).sendEmailWithAttachments(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("HU1: Validar que reporte incluye nombre, número de cuenta, fechas, transacciones y saldo")
    void should_IncludeAllRequiredDataInReport() {
        // Arrange
        TipoTransaccion tipoTransaccion = new TipoTransaccion();
        tipoTransaccion.setId(1);
        tipoTransaccion.setNombre("TRANSFERENCIA");

        Transaccion transaccion = Transaccion.builder()
                .idTransaccion(UUID.randomUUID())
                .cuentaOrigen(cuenta)
                .cuentaDestino(cuenta)
                .tipo(tipoTransaccion)
                .monto(BigDecimal.valueOf(100000))
                .fechaHora(LocalDateTime.of(2026, 1, 15, 10, 30))
                .build();

        Pageable pageable = PageRequest.of(0, 100);
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(transaccionRepository.findHistorialByCuentaAndFechaHoraBetween(
                eq(cuentaId), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(transaccion), pageable, 1));

        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDFReporte(any())).thenReturn(new byte[]{1});
        when(reporteGeneratorService.generarCSVReporte(any())).thenReturn(new byte[]{2});

        // Act
        reporteService.generarReporteMovimientos(cuentaId, fechaInicio, fechaFin);

        // Assert
        ArgumentCaptor<ReporteMovimientosDTO> captor = ArgumentCaptor.forClass(ReporteMovimientosDTO.class);
        verify(reporteGeneratorService, times(1)).generarPDFReporte(captor.capture());

        ReporteMovimientosDTO reporteCapturado = captor.getValue();
        assertEquals("Juan Pérez", reporteCapturado.getNombreCliente());
        assertEquals("12345678", reporteCapturado.getNumeroCuenta());
        assertEquals(fechaInicio, reporteCapturado.getFechaInicio());
        assertEquals(fechaFin, reporteCapturado.getFechaFin());
        assertEquals(BigDecimal.valueOf(500000), reporteCapturado.getSaldoFinal());
        assertNotNull(reporteCapturado.getTransacciones());
    }

    @Test
    @DisplayName("HU1: Enviar reporte por correo exitosamente")
    void should_SendReportByEmail_When_ReportIsGenerated() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 100);
        when(commonService.obtenerUsuarioAutenticado()).thenReturn(usuario);
        when(commonService.obtenerCuentaConValidacion(cuentaId)).thenReturn(cuenta);
        doNothing().when(commonService).validarPropietarioCuenta(cuenta, usuario);
        when(transaccionRepository.findHistorialByCuentaAndFechaHoraBetween(
                eq(cuentaId), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        when(commonService.construirNombreCompleto(usuario)).thenReturn("Juan Pérez");
        when(commonService.formatearNumeroCuenta(cuentaId)).thenReturn("12345678");
        when(reporteGeneratorService.generarPDFReporte(any())).thenReturn(new byte[]{1});
        when(reporteGeneratorService.generarCSVReporte(any())).thenReturn(new byte[]{2});

        // Act
        reporteService.generarReporteMovimientos(cuentaId, fechaInicio, fechaFin);

        // Assert
        verify(mailService, times(1)).sendEmailWithAttachments(
                eq("usuario@example.com"),
                anyString(),
                anyString(),
                any());
    }

}
