package co.edu.udea.bancodigital.unit.services;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
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
import co.edu.udea.bancodigital.services.CuentaService;

import static org.junit.jupiter.api.Assertions.*;
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
        usuario = Usuario.builder().correo("juan@example.com").build();
        SecurityContextImpl context = new SecurityContextImpl();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        when(authentication.getName()).thenReturn("juan@example.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("CP- CSD - 01: Consulta exitosa")
    void consultarSaldoCuenta_deberiaRetornarSaldoCuandoCuentaPerteneceAlUsuario() {
        when(estadoCuenta.getNombre()).thenReturn("ACTIVA");
        Cuenta cuenta = crearCuentaValida(new BigDecimal("1234.56"), usuario, estadoCuenta);

        when(usuarioRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaConDueno(cuenta.getIdCuenta())).thenReturn(Optional.of(cuenta));

        ConsultarSaldoResponse response = cuentaService.consultarSaldoCuenta(cuenta.getIdCuenta());

        assertEquals(cuenta.getIdCuenta(), response.getIdCuenta());
        assertEquals(cuenta.getSaldo(), response.getSaldo());
        assertNotNull(response.getConsultedAt());
    }

    @Test
    @DisplayName("CP- CSD - 02: Cuenta sin movimientos")
    void consultarSaldoCuenta_deberiaRetornarSaldoCeroCuandoCuentaSinMovimientos() {
        when(estadoCuenta.getNombre()).thenReturn("ACTIVA");
        Cuenta cuenta = crearCuentaValida(new BigDecimal("0.00"), usuario, estadoCuenta);

        when(usuarioRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaConDueno(cuenta.getIdCuenta())).thenReturn(Optional.of(cuenta));

        ConsultarSaldoResponse response = cuentaService.consultarSaldoCuenta(cuenta.getIdCuenta());

        assertEquals(cuenta.getIdCuenta(), response.getIdCuenta());
        assertEquals(cuenta.getSaldo(), response.getSaldo());
        assertNotNull(response.getConsultedAt());
    }

    @Test
    @DisplayName("CP- CSD - 03: Usuario sin autenticación")
    void consultarSaldoCuenta_deberiaLanzarEntityNotFoundExceptionCuandoNoEncuentraDueno() {
        UUID idCuenta = UUID.randomUUID();

        when(usuarioRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaConDueno(idCuenta)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cuentaService.consultarSaldoCuenta(idCuenta));
    }


    private static Cuenta crearCuentaValida(BigDecimal saldo, Usuario dueno, EstadoCuenta estadoCuenta) {
        return Cuenta.builder()
                .idCuenta(UUID.randomUUID())
                .dueno(dueno)
            .estadoCuenta(estadoCuenta)
                .saldo(saldo)
                .build();

        }
}
