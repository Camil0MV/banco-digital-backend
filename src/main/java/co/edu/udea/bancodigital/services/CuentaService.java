package co.edu.udea.bancodigital.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udea.bancodigital.dtos.requests.CrearCuentaRequest;
import co.edu.udea.bancodigital.dtos.responses.ConsultarCuentasResponse;
import co.edu.udea.bancodigital.dtos.responses.ConsultarCuentasResponse.DetalleCuenta;
import co.edu.udea.bancodigital.dtos.responses.ConsultarSaldoResponse;
import co.edu.udea.bancodigital.dtos.responses.CrearCuentaResponse;
import co.edu.udea.bancodigital.dtos.responses.ListarCuentasAdminResponse;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.catalogs.EstadoCuenta;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoCuenta;
import co.edu.udea.bancodigital.repositories.CuentaRepository;
import co.edu.udea.bancodigital.repositories.EstadoCuentaRepository;
import co.edu.udea.bancodigital.repositories.TipoCuentaRepository;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoCuentaRepository tipoCuentaRepository;
    private final EstadoCuentaRepository estadoCuentaRepository;

    @Transactional
    public CrearCuentaResponse crearCuenta(CrearCuentaRequest request) {
        Usuario usuario = getAuthenticatedUsuario();
        TipoCuenta tipoCuenta = getTipoCuenta(request.getIdTipoCuenta());
        EstadoCuenta estadoActiva = getEstadoActiva();

        Cuenta guardada = cuentaRepository.save(toCuenta(usuario, tipoCuenta, estadoActiva));
        return toCrearCuentaResponse(guardada);
    }

    @Transactional(readOnly = true)
    public ConsultarCuentasResponse consultarMisCuentas() {
        String correo = getAuthenticatedCorreo();
        List<Cuenta> cuentas = cuentaRepository.findAllByDuenoCorreo(correo);

        if (cuentas.isEmpty()) {
            throw new EntityNotFoundException("Cuenta no disponible");
        }

        return ConsultarCuentasResponse.builder()
                .totalCuentas(cuentas.size())
                .cuentas(cuentas.stream()
                        .map(this::toDetalleCuenta)
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public ConsultarSaldoResponse consultarSaldoCuenta(UUID idCuenta) {
        Usuario usuario = getAuthenticatedUsuario();
        Cuenta cuenta = findCuentaOrThrow(idCuenta);

        verifyOwnership(cuenta, usuario);

        return ConsultarSaldoResponse.builder()
                .idCuenta(cuenta.getIdCuenta())
                .saldo(cuenta.getSaldo())
                .estadoCuenta(cuenta.getEstadoCuenta().getNombre())
                .consultedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ListarCuentasAdminResponse> listarCuentasAdmin() {
        return cuentaRepository.findAll().stream()
                .map(this::toListarCuentasAdminResponse)
                .toList();
    }

    private String getAuthenticatedCorreo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private Usuario getAuthenticatedUsuario() {
        return usuarioRepository.findByCorreo(getAuthenticatedCorreo())
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
    }

    private TipoCuenta getTipoCuenta(Integer idTipoCuenta) {
        return tipoCuentaRepository.findById(idTipoCuenta)
                .orElseThrow(() -> new EntityNotFoundException("No existe tipo_cuenta con id: " + idTipoCuenta));
    }

    private EstadoCuenta getEstadoActiva() {
        return estadoCuentaRepository.findByNombreIgnoreCase(ESTADO_ACTIVA)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe el estado ACTIVA en la tabla estados_cuenta"));
    }

    private Cuenta toCuenta(Usuario usuario, TipoCuenta tipoCuenta, EstadoCuenta estadoActiva) {
        return Cuenta.builder()
                .dueno(usuario)
                .tipoCuenta(tipoCuenta)
                .estadoCuenta(estadoActiva)
                .saldo(BigDecimal.ZERO)
                .build();
    }

    private CrearCuentaResponse toCrearCuentaResponse(Cuenta cuenta) {
        return CrearCuentaResponse.builder()
                .idCuenta(cuenta.getIdCuenta())
                .idTipoCuenta(cuenta.getTipoCuenta().getId())
                .tipoCuenta(cuenta.getTipoCuenta().getNombre())
                .idEstadoCuenta(cuenta.getEstadoCuenta().getId())
                .estadoCuenta(cuenta.getEstadoCuenta().getNombre())
                .saldo(cuenta.getSaldo())
                .createdAt(cuenta.getCreatedAt())
                .build();
    }

    private DetalleCuenta toDetalleCuenta(Cuenta cuenta) {
        return DetalleCuenta.builder()
                .idCuenta(cuenta.getIdCuenta())
                .idTipoCuenta(cuenta.getTipoCuenta().getId())
                .tipoCuenta(cuenta.getTipoCuenta().getNombre())
                .idEstadoCuenta(cuenta.getEstadoCuenta().getId())
                .estadoCuenta(cuenta.getEstadoCuenta().getNombre())
                .saldo(cuenta.getSaldo())
                .createdAt(cuenta.getCreatedAt())
                .build();
    }

    private Cuenta findCuentaOrThrow(UUID idCuenta) {
        return cuentaRepository.findByIdCuentaConDueno(idCuenta)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cuenta con id " + idCuenta + " no existe"));
    }

    private void verifyOwnership(Cuenta cuenta, Usuario usuario) {
        if (!cuenta.getDueno().equals(usuario)) {
            throw new AccessDeniedException("No tiene permisos para acceder a esta cuenta");
        }
    }

    private ListarCuentasAdminResponse toListarCuentasAdminResponse(Cuenta cuenta) {
        return ListarCuentasAdminResponse.builder()
                .idCuenta(cuenta.getIdCuenta())
                .tipoDocumentoDueno(cuenta.getDueno().getTipoDocumento().getNombre())
                .numeroDocumentoDueno(cuenta.getDueno().getId().getNumeroDocumento())
                .nombreCompletoDueno(buildNombreCompletoDueno(cuenta.getDueno()))
                .tipoCuenta(cuenta.getTipoCuenta().getNombre())
                .estadoCuenta(cuenta.getEstadoCuenta().getNombre())
                .saldo(cuenta.getSaldo())
                .createdAt(cuenta.getCreatedAt())
                .build();
    }

    private String buildNombreCompletoDueno(Usuario dueno) {
        String segundoApellido = dueno.getSegundoApellido();
        if (segundoApellido == null || segundoApellido.isBlank()) {
            return dueno.getNombre() + " " + dueno.getPrimerApellido();
        }
        return dueno.getNombre() + " " + dueno.getPrimerApellido() + " " + segundoApellido;
    }
}
