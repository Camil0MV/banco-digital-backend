package co.edu.udea.bancodigital.services;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import co.edu.udea.bancodigital.dtos.responses.ConsultarCuentasResponse;
import co.edu.udea.bancodigital.dtos.responses.ConsultarSaldoResponse;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.catalogs.EstadoCuenta;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoCuenta;
import co.edu.udea.bancodigital.repositories.CuentaRepository;
import co.edu.udea.bancodigital.repositories.EstadoCuentaRepository;
import co.edu.udea.bancodigital.repositories.TipoCuentaRepository;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoCuentaRepository tipoCuentaRepository;

    @Mock
    private EstadoCuentaRepository estadoCuentaRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private TipoCuenta tipoCuenta;

    @Mock  
    private EstadoCuenta estadoCuenta;

    @InjectMocks
    private CuentaService cuentaService;

    private Usuario usuario;

    @BeforeEach
    void setUpSecurityContext() {
        usuario = Usuario.builder().correo("user@example.com").build();
        SecurityContextImpl context = new SecurityContextImpl();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        when(authentication.getName()).thenReturn("user@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void consultarSaldoCuenta_deberiaRetornarSaldoCuandoCuentaPerteneceAlUsuario() {
        Usuario usuario = Usuario.builder().correo("user@example.com").build();
        Cuenta cuenta = Cuenta.builder()
                .idCuenta(UUID.randomUUID())
                .dueno(usuario)
                .saldo(new BigDecimal("1234.56"))
                .build();

        when(usuarioRepository.findByCorreo("user@example.com")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findById(cuenta.getIdCuenta())).thenReturn(Optional.of(cuenta));

        ConsultarSaldoResponse response = cuentaService.consultarSaldoCuenta(cuenta.getIdCuenta());

        assertEquals(cuenta.getIdCuenta(), response.getIdCuenta());
        assertEquals(cuenta.getSaldo(), response.getSaldo());
        assertNotNull(response.getConsultedAt());
    }

    @Test
    void consultarSaldoCuenta_deberiaLanzarExcepcionCuandoCuentaNoExiste() {
        Usuario usuario = Usuario.builder().correo("user@example.com").build();

        when(usuarioRepository.findByCorreo("user@example.com")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cuentaService.consultarSaldoCuenta(UUID.randomUUID()));
    }

    @Test
    void consultarSaldoCuenta_deberiaLanzarAccessDeniedCuandoNoEsDueno() {
        Usuario otroDueno = Usuario.builder().correo("otro@example.com").build();
        Cuenta cuenta = Cuenta.builder()
            .idCuenta(UUID.randomUUID())
            .dueno(otroDueno)
            .saldo(new BigDecimal("1000.00"))
            .build();

        when(usuarioRepository.findByCorreo("user@example.com"))
            .thenReturn(Optional.of(usuario));
        when(cuentaRepository.findById(cuenta.getIdCuenta()))
            .thenReturn(Optional.of(cuenta));

        assertThrows(AccessDeniedException.class,
            () -> cuentaService.consultarSaldoCuenta(cuenta.getIdCuenta()));
    }

    @Test
    void consultarMisCuentas_deberiaRetornarListaDeCuentas() {
        when(tipoCuenta.getId()).thenReturn(1);
        when(tipoCuenta.getNombre()).thenReturn("AHORROS");
        when(estadoCuenta.getId()).thenReturn(1);
        when(estadoCuenta.getNombre()).thenReturn("ACTIVA");

        Cuenta cuenta = Cuenta.builder()
            .idCuenta(UUID.randomUUID())
            .dueno(usuario)
            .saldo(new BigDecimal("500.00"))
            .tipoCuenta(tipoCuenta)
            .estadoCuenta(estadoCuenta)
            .build();

        when(usuarioRepository.findByCorreo("user@example.com"))
            .thenReturn(Optional.of(usuario));
        when(cuentaRepository.findAllByDueno(usuario))
            .thenReturn(List.of(cuenta));

        ConsultarCuentasResponse response = cuentaService.consultarMisCuentas();

        assertEquals(1, response.getTotalCuentas());
        assertFalse(response.getCuentas().isEmpty());
    }

    @Test
    void consultarMisCuentas_deberiaRetornarListaVaciaSiNoTieneCuentas() {
        when(usuarioRepository.findByCorreo("user@example.com"))
            .thenReturn(Optional.of(usuario));
        when(cuentaRepository.findAllByDueno(usuario))
            .thenReturn(List.of());

        ConsultarCuentasResponse response = cuentaService.consultarMisCuentas();

        assertEquals(0, response.getTotalCuentas());
        assertTrue(response.getCuentas().isEmpty());
    }
}
