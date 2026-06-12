package co.edu.udea.bancodigital.controllers;

import co.edu.udea.bancodigital.dtos.requests.GenerarReporteRequest;
import co.edu.udea.bancodigital.services.CertificadoService;
import co.edu.udea.bancodigital.services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para generar reportes y certificados bancarios")
public class ReporteController {

    private final ReporteService reporteService;
    private final CertificadoService certificadoService;

    @PostMapping("/certificado/{idCuenta}")
    @Operation(summary = "Solicitar reporte del certificado bancario",
            description = "Solicita un reporte del certificado bancario que será enviado por correo en formato PDF y CSV",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Reporte del certificado solicitado exitosamente",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"success\": true, \"message\": \"Reporte del certificado enviado a tu correo\"}")))
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - no eres propietario de la cuenta")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    @ApiResponse(responseCode = "500", description = "Error generando o enviando reporte")
    public ResponseEntity<Map<String, Object>> solicitarReporteCertificadoBancario(
            @PathVariable UUID idCuenta) {

        certificadoService.solicitarReporteCertificadoBancario(idCuenta);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reporte del certificado bancario solicitado exitosamente. Te lo enviaremos por correo en breve.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/movimientos/{idCuenta}")
    @Operation(summary = "Generar reporte de movimientos en un rango de fechas",
            description = "Genera un reporte de transacciones en PDF y CSV para un rango de fechas (máximo 90 días) y lo envía por email",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Reporte generado y enviado exitosamente",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"success\": true, \"message\": \"Reporte generado y enviado a tu correo\"}")))
    @ApiResponse(responseCode = "400", description = "El rango de fechas no puede ser superior a 90 días")
    @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado - no eres propietario de la cuenta")
    @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    @ApiResponse(responseCode = "500", description = "Error generando o enviando reporte")
    public ResponseEntity<Map<String, Object>> generarReporteMovimientos(
            @PathVariable UUID idCuenta,
            @Valid @RequestBody GenerarReporteRequest request) {

        reporteService.generarReporteMovimientos(idCuenta, request.getFechaInicio(), request.getFechaFin());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reporte generado y enviado a tu correo");

        return ResponseEntity.ok(response);
    }
}
