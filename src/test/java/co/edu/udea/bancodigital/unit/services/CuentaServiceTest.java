package co.edu.udea.bancodigital.unit.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import co.edu.udea.bancodigital.dtos.requests.CrearCuentaRequest;
import co.edu.udea.bancodigital.dtos.responses.ConsultarSaldoResponse;
import co.edu.udea.bancodigital.dtos.responses.CrearCuentaResponse;
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
        Cuenta cuenta = crearCuentaValida(UUID.randomUUID(), usuario, null, estadoCuenta, new BigDecimal("1234.56"));

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
        Cuenta cuenta = crearCuentaValida(UUID.randomUUID(), usuario, null, estadoCuenta, new BigDecimal("0.00"));

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

    @Test
    @DisplayName("Crear cuenta satisfactoriamente")
    void crearCuenta_deberiaCrearCuentaConDatosValidos() throws Exception {
        CrearCuentaRequest request = crearRequestConTipoCuenta(1);
        TipoCuenta tipoCuenta1 = new TipoCuenta();
        tipoCuenta1.setId(1);
        tipoCuenta1.setNombre("Ahorros");
        EstadoCuenta estado = new EstadoCuenta();
        estado.setId(1);
        estado.setNombre("ACTIVA");
        Cuenta guardada = crearCuentaValida(UUID.randomUUID(), usuario, tipoCuenta1, estado, BigDecimal.ZERO);
        guardada.setCreatedAt(LocalDateTime.now());

        when(authentication.getName()).thenReturn("juan@example.com");
        when(usuarioRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        when(tipoCuentaRepository.findById(1)).thenReturn(Optional.of(tipoCuenta1));
        when(estadoCuentaRepository.findByNombreIgnoreCase("ACTIVA")).thenReturn(Optional.of(estado));
        when(cuentaRepository.save(org.mockito.Mockito.any(Cuenta.class))).thenReturn(guardada);

        CrearCuentaResponse response = cuentaService.crearCuenta(request);

        assertEquals(guardada.getIdCuenta(), response.getIdCuenta());
        assertEquals(tipoCuenta1.getNombre(), response.getTipoCuenta());
        assertEquals(estado.getNombre(), response.getEstadoCuenta());
        assertEquals(BigDecimal.ZERO, response.getSaldo());
        assertNotNull(response.getCreatedAt());
    }

    private static Cuenta crearCuentaValida(UUID idCuenta, Usuario dueno, TipoCuenta tipoCuenta, EstadoCuenta estadoCuenta, BigDecimal saldo) {
        return Cuenta.builder()
                .idCuenta(idCuenta)
                .dueno(dueno)
                .tipoCuenta(tipoCuenta)
                .estadoCuenta(estadoCuenta)
                .saldo(saldo)
                .build();
    }

    private static CrearCuentaRequest crearRequestConTipoCuenta(int idTipoCuenta) throws Exception {
        CrearCuentaRequest request = new CrearCuentaRequest();
        java.lang.reflect.Field field = CrearCuentaRequest.class.getDeclaredField("idTipoCuenta");
        field.setAccessible(true);
        field.set(request, idTipoCuenta);
        return request;
    }
}
