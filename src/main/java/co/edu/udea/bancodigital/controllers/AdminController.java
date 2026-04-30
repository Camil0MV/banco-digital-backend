package co.edu.udea.bancodigital.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.udea.bancodigital.dtos.responses.ListarClientesResponse;
import co.edu.udea.bancodigital.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final UsuarioService usuarioService;

    @GetMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar clientes registrados", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Lista de clientes registrada")
    @ApiResponse(responseCode = "401", description = "Token JWT invalido o expirado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<CollectionModel<ListarClientesResponse>> listarClientes() {
        CollectionModel<ListarClientesResponse> model = CollectionModel.of(
                usuarioService.listarClientes(),
                linkTo(methodOn(AdminController.class).listarClientes()).withSelfRel());
        return ResponseEntity.ok(model);
    }
}
