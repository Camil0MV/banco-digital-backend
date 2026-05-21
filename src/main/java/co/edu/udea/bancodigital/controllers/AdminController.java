package co.edu.udea.bancodigital.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.udea.bancodigital.dtos.responses.ListarClientesAdminResponse;
import co.edu.udea.bancodigital.dtos.responses.ListarCuentasAdminResponse;
import co.edu.udea.bancodigital.dtos.responses.AuditoriaTransaccionesResponse;
import co.edu.udea.bancodigital.services.CuentaService;
import co.edu.udea.bancodigital.services.UsuarioService;
import co.edu.udea.bancodigital.services.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints para administración del sistema y gestión de clientes")
public class AdminController {

    private final UsuarioService usuarioService;
    private final CuentaService cuentaService;
    private final TransaccionService transaccionService;

    @GetMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar clientes registrados", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Lista de clientes registrada")
    @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public ResponseEntity<CollectionModel<ListarClientesAdminResponse>> listarClientes() {
        CollectionModel<ListarClientesAdminResponse> model = CollectionModel.of(
                usuarioService.listarClientes(),
                linkTo(methodOn(AdminController.class).listarClientes()).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @GetMapping("/cuentas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar cuentas del sistema", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Lista de cuentas registrada")
    @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public ResponseEntity<CollectionModel<ListarCuentasAdminResponse>> listarCuentas() {
        CollectionModel<ListarCuentasAdminResponse> model = CollectionModel.of(
                cuentaService.listarCuentasAdmin(),
                linkTo(methodOn(AdminController.class).listarCuentas()).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @GetMapping("/transacciones")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar historial completo de transacciones", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Historial de transacciones consultado exitosamente")
    @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado: permisos insuficientes")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public ResponseEntity<AuditoriaTransaccionesResponse> listarTransaccionesAuditoria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new IllegalArgumentException("El numero de pagina debe ser cero o mayor");
        }
        Pageable pageable = PageRequest.of(page, normalizarSize(size));
        return ResponseEntity.ok(transaccionService.consultarHistorialAuditoria(pageable));
    }

    private int normalizarSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("El tamano de pagina debe ser mayor a cero");
        }
        return Math.min(size, 100);
    }
}
