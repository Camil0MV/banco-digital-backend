package co.edu.udea.bancodigital.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udea.bancodigital.dtos.requests.CrearCuentaRequest;
import co.edu.udea.bancodigital.dtos.responses.ConsultarCuentasResponse;
import co.edu.udea.bancodigital.dtos.responses.ConsultarCuentasResponse.DetalleCuenta;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();
        
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));

        TipoCuenta tipoCuenta = tipoCuentaRepository.findById(request.getIdTipoCuenta())
                .orElseThrow(() -> new EntityNotFoundException("No existe tipo_cuenta con id: " + request.getIdTipoCuenta()));

        EstadoCuenta estadoActiva = estadoCuentaRepository.findByNombreIgnoreCase(ESTADO_ACTIVA)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No existe el estado ACTIVA en la tabla estados_cuenta"));

        Cuenta cuenta = Cuenta.builder()
                .dueno(usuario)
                .tipoCuenta(tipoCuenta)
                .estadoCuenta(estadoActiva)
                .saldo(BigDecimal.ZERO)
                .build();

        Cuenta guardada = cuentaRepository.save(cuenta);

        return CrearCuentaResponse.builder()
                .idCuenta(guardada.getIdCuenta())
                .idTipoCuenta(guardada.getTipoCuenta().getId())
                .tipoCuenta(guardada.getTipoCuenta().getNombre())
                .idEstadoCuenta(guardada.getEstadoCuenta().getId())
                .estadoCuenta(guardada.getEstadoCuenta().getNombre())
                .saldo(guardada.getSaldo())
                .createdAt(guardada.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ConsultarCuentasResponse consultarMisCuentas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));

        List<Cuenta> cuentas = cuentaRepository.findAllByDueno(usuario);

        List<DetalleCuenta> detalles = cuentas.stream()
                .map(c -> DetalleCuenta.builder()
                        .idCuenta(c.getIdCuenta())
                        .idTipoCuenta(c.getTipoCuenta().getId())
                        .tipoCuenta(c.getTipoCuenta().getNombre())
                        .idEstadoCuenta(c.getEstadoCuenta().getId())
                        .estadoCuenta(c.getEstadoCuenta().getNombre())
                        .saldo(c.getSaldo())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();

        return ConsultarCuentasResponse.builder()
                .totalCuentas(cuentas.size())
                .cuentas(detalles)
                .build();
    }

    @Transactional(readOnly = true)
    public ConsultarSaldoResponse consultarSaldoCuenta(UUID idCuenta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));

        Cuenta cuenta = cuentaRepository.findByIdCuentaAndDueno(idCuenta, usuario)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta con id " + idCuenta + " no existe o no pertenece al usuario autenticado"));

        return ConsultarSaldoResponse.builder()
                .idCuenta(cuenta.getIdCuenta())
                .saldo(cuenta.getSaldo())
                .consultedAt(LocalDateTime.now())
                .build();
    }
}
