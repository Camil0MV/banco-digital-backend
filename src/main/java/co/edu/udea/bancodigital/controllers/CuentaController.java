package co.edu.udea.bancodigital.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.udea.bancodigital.dtos.requests.CrearCuentaRequest;
import co.edu.udea.bancodigital.dtos.responses.ConsultarCuentasResponse;
import co.edu.udea.bancodigital.dtos.responses.ConsultarSaldoResponse;
import co.edu.udea.bancodigital.dtos.responses.CrearCuentaResponse;
import co.edu.udea.bancodigital.services.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    @Operation(summary = "Crear una nueva cuenta bancaria para el usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CrearCuentaResponse> crearCuenta(@Valid @RequestBody CrearCuentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.crearCuenta(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar las cuentas del usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ConsultarCuentasResponse> consultarMisCuentas() {
        return ResponseEntity.ok(cuentaService.consultarMisCuentas());
    }

    @GetMapping("/{idCuenta}/saldo")
    @Operation(summary = "Consultar el saldo disponible de una cuenta", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ConsultarSaldoResponse> consultarSaldoCuenta(@PathVariable UUID idCuenta) {
        return ResponseEntity.ok(cuentaService.consultarSaldoCuenta(idCuenta));
    }
}
