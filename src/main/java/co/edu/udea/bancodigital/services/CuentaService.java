package co.edu.udea.bancodigital.services;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import co.edu.udea.bancodigital.dtos.requests.CrearCuentaRequest;
import co.edu.udea.bancodigital.dtos.responses.CrearCuentaResponse;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.catalogs.EstadoCuenta;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoCuenta;
import co.edu.udea.bancodigital.repositories.CuentaRepository;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntityManager entityManager;

    public CrearCuentaResponse crearCuenta(CrearCuentaRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        String correo = authentication.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));

        TipoCuenta tipoCuenta = entityManager.find(TipoCuenta.class, request.getIdTipoCuenta());
        if (tipoCuenta == null) {
            throw new IllegalArgumentException("No existe tipo_cuenta con id: " + request.getIdTipoCuenta());
        }

        EstadoCuenta estadoActiva = findEstadoActiva();

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

    private EstadoCuenta findEstadoActiva() {
        return entityManager.createQuery(
                        "select e from EstadoCuenta e where upper(e.nombre) = :nombre", EstadoCuenta.class)
                .setParameter("nombre", ESTADO_ACTIVA)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new DataIntegrityViolationException(
                        "No existe el estado ACTIVA en la tabla estados_cuenta"));
    }
}
