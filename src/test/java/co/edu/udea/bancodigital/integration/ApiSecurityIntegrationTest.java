package co.edu.udea.bancodigital.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.udea.bancodigital.dtos.requests.LoginRequest;
import co.edu.udea.bancodigital.dtos.requests.RegistroRequest;
import co.edu.udea.bancodigital.dtos.responses.ConsultarCuentasResponse;
import co.edu.udea.bancodigital.dtos.responses.ConsultarSaldoResponse;
import co.edu.udea.bancodigital.dtos.responses.ListarClientesAdminResponse;
import co.edu.udea.bancodigital.dtos.responses.ListarCuentasAdminResponse;
import co.edu.udea.bancodigital.dtos.responses.LoginResponse;
import co.edu.udea.bancodigital.dtos.responses.RegistroResponse;
import co.edu.udea.bancodigital.dtos.responses.AuditoriaTransaccionResponse;
import co.edu.udea.bancodigital.dtos.responses.AuditoriaTransaccionesResponse;
import co.edu.udea.bancodigital.services.AuthService;
import co.edu.udea.bancodigital.services.CuentaService;
import co.edu.udea.bancodigital.services.UsuarioService;
import co.edu.udea.bancodigital.services.TransaccionService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.jwt.secret=test-secret-test-secret-test-secret-test-secret",
    "app.jwt.expiration=86400000"
})
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Flyway flyway;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CuentaService cuentaService;

        @MockitoBean
        private TransaccionService transaccionService;

    @Test
    @DisplayName("Integracion CP-REG-01: registro publico retorna 201")
    void registrarUsuarioPublico_deberiaRetornarCreated() throws Exception {
        when(usuarioService.registrar(any(RegistroRequest.class))).thenReturn(RegistroResponse.builder()
                .idTipoDoc(1)
                .tipoDocumento("CC")
                .numeroDocumento("****7890")
                .nombre("Juan")
                .primerApellido("Perez")
                .correo("juan@example.com")
                .idRol(2)
                .rol("CLIENTE")
                .build());

        mockMvc.perform(post("/api/v1/usuarios/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "idTipoDoc": 1,
                          "numeroDocumento": "1234567890",
                          "nombre": "Juan",
                          "primerApellido": "Perez",
                          "segundoApellido": "Gomez",
                          "direccion": "Calle 123",
                          "telefono": "3001234567",
                          "correo": "juan@example.com",
                          "contrasena": "Abc123#@"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("juan@example.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    @DisplayName("Integracion CP-REG-05: registro con email invalido retorna 400")
    void registrarUsuarioConEmailInvalido_deberiaRetornarBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "idTipoDoc": 1,
                          "numeroDocumento": "1234567890",
                          "nombre": "Juan",
                          "primerApellido": "Perez",
                          "direccion": "Calle 123",
                          "telefono": "3001234567",
                          "correo": "juan.gmail.com",
                          "contrasena": "Abc123#@"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integracion CP-LOG-01: login publico retorna token")
    void loginPublico_deberiaRetornarOk() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(LoginResponse.builder()
                .token("jwt-token")
                .tipo("Bearer")
                .nombre("Juan")
                .correo("juan@example.com")
                .idRol(2)
                .rol("CLIENTE")
                .build());

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "correo": "juan@example.com",
                          "contrasena": "Abc123#@"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integracion CP-ADM-01: admin autenticado puede listar clientes")
    void listarClientesComoAdmin_deberiaRetornarOk() throws Exception {
        when(usuarioService.listarClientes()).thenReturn(List.of(ListarClientesAdminResponse.builder()
                .idTipoDocumento(1)
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombre("Juan")
                .primerApellido("Perez")
                .correo("juan@example.com")
                .telefono("3001234567")
                .direccion("Calle 123")
                .rol("CLIENTE")
                .createdAt(LocalDateTime.now())
                .build()));

        mockMvc.perform(get("/api/v1/admin/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.listarClientesAdminResponseList", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integracion CP-ADM-10: admin autenticado puede auditar transacciones")
    void listarTransaccionesAuditoriaComoAdmin_deberiaRetornarOk() throws Exception {
        when(transaccionService.consultarHistorialAuditoria(any()))
                .thenReturn(AuditoriaTransaccionesResponse.builder()
                        .transacciones(List.of(AuditoriaTransaccionResponse.builder()
                                .idTransaccion(UUID.randomUUID())
                                .cliente("Juan Perez")
                                .cuentaOrigen(UUID.randomUUID())
                                .cuentaDestino(UUID.randomUUID())
                                .fechaHora(LocalDateTime.now())
                                .monto(new BigDecimal("1500.00"))
                                .estado("COMPLETADA")
                                .descripcion("Transferencia entre cuentas")
                                .tipoTransaccion("TRANSFERENCIA")
                                .build()))
                        .total(1)
                        .pagina(0)
                        .tamanoPagina(20)
                        .totalPaginas(1)
                        .tieneDatos(true)
                        .build());

        mockMvc.perform(get("/api/v1/admin/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transacciones", hasSize(1)))
                .andExpect(jsonPath("$.transacciones[0].cliente").value("Juan Perez"));
    }

    @Test
    @DisplayName("Integracion CP-ADM-11: usuario sin autenticacion no puede auditar transacciones")
    void listarTransaccionesAuditoriaSinAutenticacion_deberiaRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/transacciones").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("No autenticado"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Integracion CP-ADM-12: cliente autenticado no puede auditar transacciones")
    void listarTransaccionesAuditoriaComoCliente_deberiaRetornarForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/transacciones"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Integracion CP-ADM-03: cliente no puede consultar admin")
    void listarClientesComoCliente_deberiaRetornarForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clientes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    @DisplayName("Integracion CP-ADMC-04: usuario sin autenticacion retorna 401")
    void listarClientesSinAutenticacion_deberiaRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clientes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("No autenticado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integracion CP-ADMC-01: admin puede listar cuentas")
    void listarCuentasComoAdmin_deberiaRetornarOk() throws Exception {
        when(cuentaService.listarCuentasAdmin()).thenReturn(List.of(ListarCuentasAdminResponse.builder()
                .idCuenta(UUID.randomUUID())
                .tipoDocumentoDueno("CC")
                .numeroDocumentoDueno("1234567890")
                .nombreCompletoDueno("Juan Perez")
                .tipoCuenta("AHORROS")
                .estadoCuenta("ACTIVA")
                .saldo(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.now())
                .build()));

        mockMvc.perform(get("/api/v1/admin/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.listarCuentasAdminResponseList", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("Integracion CP-ADMC-03: cliente no puede consultar cuentas admin")
    void listarCuentasComoCliente_deberiaRetornarForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/cuentas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("Integracion CP-ACC-01: usuario autenticado puede consultar sus cuentas")
    void consultarMisCuentasAutenticado_deberiaRetornarOk() throws Exception {
        when(cuentaService.consultarMisCuentas()).thenReturn(ConsultarCuentasResponse.builder()
                .totalCuentas(1)
                .cuentas(List.of(ConsultarCuentasResponse.DetalleCuenta.builder()
                        .idCuenta(UUID.randomUUID())
                        .idTipoCuenta(1)
                        .tipoCuenta("AHORROS")
                        .idEstadoCuenta(1)
                        .estadoCuenta("ACTIVA")
                        .saldo(BigDecimal.ZERO)
                        .createdAt(LocalDateTime.now())
                        .build()))
                .build());

        mockMvc.perform(get("/api/v1/cuentas/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detalleCuentaList", hasSize(1)));
    }

    @Test
    @DisplayName("Integracion CP-ACC-02: usuario sin autenticacion retorna 401")
    void consultarMisCuentasSinAutenticacion_deberiaRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cuentas/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("No autenticado"));
    }

    @Test
    @WithMockUser
    @DisplayName("Integracion CP-CSD-01: usuario autenticado puede consultar saldo")
    void consultarSaldoAutenticado_deberiaRetornarOk() throws Exception {
        UUID idCuenta = UUID.randomUUID();
        when(cuentaService.consultarSaldoCuenta(idCuenta)).thenReturn(ConsultarSaldoResponse.builder()
                .idCuenta(idCuenta)
                .saldo(new BigDecimal("500.00"))
                .estadoCuenta("ACTIVA")
                .consultedAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/cuentas/{idCuenta}/saldo", idCuenta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(500.00))
                .andExpect(jsonPath("$.estadoCuenta").value("ACTIVA"));
    }

    @Test
    @DisplayName("Integracion CP-CSD-03: usuario sin autenticacion retorna 401")
    void consultarSaldoSinAutenticacion_deberiaRetornarUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cuentas/{idCuenta}/saldo", UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("No autenticado"));
    }
}
