package co.edu.udea.bancodigital.services;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import co.edu.udea.bancodigital.dtos.responses.ConsultarSaldoResponse;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.repositories.CuentaRepository;
import co.edu.udea.bancodigital.repositories.EstadoCuentaRepository;
import co.edu.udea.bancodigital.repositories.TipoCuentaRepository;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @InjectMocks
    private CuentaService cuentaService;

    @BeforeEach
    void setUpSecurityContext() {
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
        when(cuentaRepository.findByIdCuentaAndDueno(cuenta.getIdCuenta(), usuario)).thenReturn(Optional.of(cuenta));

        ConsultarSaldoResponse response = cuentaService.consultarSaldoCuenta(cuenta.getIdCuenta());

        assertEquals(cuenta.getIdCuenta(), response.getIdCuenta());
        assertEquals(cuenta.getSaldo(), response.getSaldo());
        assertNotNull(response.getConsultedAt());
    }

    @Test
    void consultarSaldoCuenta_deberiaLanzarExcepcionCuandoCuentaNoExiste() {
        Usuario usuario = Usuario.builder().correo("user@example.com").build();

        when(usuarioRepository.findByCorreo("user@example.com")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndDueno(any(UUID.class), eq(usuario))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cuentaService.consultarSaldoCuenta(UUID.randomUUID()));
    }
}
