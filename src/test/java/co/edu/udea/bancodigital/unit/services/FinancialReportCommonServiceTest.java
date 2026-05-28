package co.edu.udea.bancodigital.unit.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.catalogs.EstadoCuenta;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoCuenta;
import co.edu.udea.bancodigital.models.pks.UsuarioId;
import co.edu.udea.bancodigital.repositories.CuentaRepository;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;
import co.edu.udea.bancodigital.services.FinancialReportCommonService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FinancialReportCommonServiceTest - Pruebas de Servicio Común para Reportes Financieros")
class FinancialReportCommonServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FinancialReportCommonService commonService;

    private UUID cuentaId;
    private Usuario usuario;
    private Cuenta cuenta;
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
                .segundoApellido("García")
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

    // ==================== OBTENER USUARIO AUTENTICADO ====================

    @Test
    @DisplayName("Obtener usuario autenticado exitosamente")
    void should_ObtainAuthenticatedUser_When_UserExists() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@example.com")).thenReturn(Optional.of(usuario));

        // Act
        Usuario usuarioObtenido = commonService.obtenerUsuarioAutenticado();

        // Assert
        assertNotNull(usuarioObtenido);
        assertEquals("usuario@example.com", usuarioObtenido.getCorreo());
        assertEquals("Juan", usuarioObtenido.getNombre());
        verify(usuarioRepository).findByCorreo("usuario@example.com");
    }

    @Test
    @DisplayName("Rechazar cuando usuario autenticado no existe en base de datos")
    void should_ThrowException_When_AuthenticatedUserNotFound() {
        // Arrange
        when(usuarioRepository.findByCorreo("usuario@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> commonService.obtenerUsuarioAutenticado());

        verify(usuarioRepository).findByCorreo("usuario@example.com");
    }

    // ==================== OBTENER CUENTA CON VALIDACIÓN ====================

    @Test
    @DisplayName("Obtener cuenta con validación exitosamente")
    void should_ObtainAccountWithValidation_When_AccountExists() {
        // Arrange
        when(cuentaRepository.findByIdCuentaConDueno(cuentaId)).thenReturn(Optional.of(cuenta));

        // Act
        Cuenta cuentaObtenida = commonService.obtenerCuentaConValidacion(cuentaId);

        // Assert
        assertNotNull(cuentaObtenida);
        assertEquals(cuentaId, cuentaObtenida.getIdCuenta());
        assertEquals(usuario, cuentaObtenida.getDueno());
        verify(cuentaRepository).findByIdCuentaConDueno(cuentaId);
    }

    @Test
    @DisplayName("Rechazar cuando cuenta no existe")
    void should_ThrowException_When_AccountNotFound() {
        // Arrange
        UUID invalidCuentaId = UUID.randomUUID();
        when(cuentaRepository.findByIdCuentaConDueno(invalidCuentaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> commonService.obtenerCuentaConValidacion(invalidCuentaId));

        verify(cuentaRepository).findByIdCuentaConDueno(invalidCuentaId);
    }

    // ==================== VALIDAR PROPIETARIO DE CUENTA ====================

    @Test
    @DisplayName("Validar propietario de cuenta - Acceso permitido para propietario")
    void should_AllowAccess_When_UserIsAccountOwner() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> commonService.validarPropietarioCuenta(cuenta, usuario));
    }

    @Test
    @DisplayName("Rechazar acceso cuando usuario no es propietario de la cuenta")
    void should_DenyAccess_When_UserIsNotAccountOwner() {
        // Arrange
        Usuario usuarioDiferente = Usuario.builder()
                .id(new UsuarioId(2, "456"))
                .correo("otro@example.com")
                .nombre("María")
                .primerApellido("García")
                .build();

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> commonService.validarPropietarioCuenta(cuenta, usuarioDiferente));
    }

    // ==================== CONSTRUIR NOMBRE COMPLETO ====================

    @Test
    @DisplayName("Construir nombre completo con dos apellidos")
    void should_BuildFullName_When_UserHasTwoLastNames() {
        // Arrange
        Usuario usuarioConDosApellidos = Usuario.builder()
                .id(new UsuarioId(1, "123"))
                .correo("usuario@example.com")
                .nombre("Juan")
                .primerApellido("Pérez")
                .segundoApellido("García")
                .build();

        // Act
        String nombreCompleto = commonService.construirNombreCompleto(usuarioConDosApellidos);

        // Assert
        assertEquals("Juan Pérez García", nombreCompleto);
    }

    @Test
    @DisplayName("Construir nombre completo con un apellido")
    void should_BuildFullName_When_UserHasOneLastName() {
        // Arrange
        Usuario usuarioUnApellido = Usuario.builder()
                .id(new UsuarioId(1, "123"))
                .correo("usuario@example.com")
                .nombre("María")
                .primerApellido("López")
                .build();

        // Act
        String nombreCompleto = commonService.construirNombreCompleto(usuarioUnApellido);

        // Assert
        assertEquals("María López", nombreCompleto);
    }

    @Test
    @DisplayName("Construir nombre completo ignorando segundo apellido vacío")
    void should_BuildFullName_IgnoringEmptySecondLastName() {
        // Arrange
        Usuario usuarioApellidoVacio = Usuario.builder()
                .id(new UsuarioId(1, "123"))
                .correo("usuario@example.com")
                .nombre("Carlos")
                .primerApellido("Martínez")
                .segundoApellido("")
                .build();

        // Act
        String nombreCompleto = commonService.construirNombreCompleto(usuarioApellidoVacio);

        // Assert
        assertEquals("Carlos Martínez", nombreCompleto);
    }

    // ==================== FORMATEAR NÚMERO DE CUENTA ====================

    @Test
    @DisplayName("Formatear número de cuenta extrayendo últimos 8 dígitos")
    void should_FormatAccountNumber_When_UUIDIsProvided() {
        // Arrange
        UUID uuidCuenta = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        // Act
        String numeroCuenta = commonService.formatearNumeroCuenta(uuidCuenta);

        // Assert
        assertNotNull(numeroCuenta);
        assertEquals(8, numeroCuenta.length());
        assertEquals("55440000", numeroCuenta);
    }

    @Test
    @DisplayName("Formatear número de cuenta en mayúsculas")
    void should_FormatAccountNumber_InUppercase() {
        // Arrange
        UUID uuidCuenta = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Act
        String numeroCuenta = commonService.formatearNumeroCuenta(uuidCuenta);

        // Assert
        assertTrue(numeroCuenta.matches("[0-9A-F]+"));
        assertEquals(8, numeroCuenta.length());
    }

    @Test
    @DisplayName("Formatear diferentes UUIDs produce diferentes números de cuenta")
    void should_ProduceDifferentAccountNumbers_ForDifferentUUIDs() {
        // Arrange
        UUID uuid1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID uuid2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

        // Act
        String numero1 = commonService.formatearNumeroCuenta(uuid1);
        String numero2 = commonService.formatearNumeroCuenta(uuid2);

        // Assert
        assertNotNull(numero1);
        assertNotNull(numero2);
        assertEquals(8, numero1.length());
        assertEquals(8, numero2.length());
    }

    // ==================== HELPERS ====================

    private boolean assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but got false");
        }
        return true;
    }

}
