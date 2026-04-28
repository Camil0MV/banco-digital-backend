package co.edu.udea.bancodigital.services;

import java.math.BigDecimal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.udea.bancodigital.dtos.requests.CrearCuentaRequest;
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


}
