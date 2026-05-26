package co.edu.udea.bancodigital.services;

import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.repositories.CuentaRepository;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialReportCommonService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;

    public Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
    }

    public Cuenta obtenerCuentaConValidacion(UUID idCuenta) {
        return cuentaRepository.findByIdCuentaConDueno(idCuenta)
                .orElseThrow(() -> new EntityNotFoundException("Cuenta con id " + idCuenta + " no existe"));
    }

    public void validarPropietarioCuenta(Cuenta cuenta, Usuario usuario) {
        if (!cuenta.getDueno().getId().equals(usuario.getId())) {
            log.warn("Intento de acceso no autorizado a cuenta {} por usuario {}",
                    cuenta.getIdCuenta(), usuario.getId());
            throw new AccessDeniedException("No tienes permisos para acceder a esta cuenta");
        }
    }

    public String construirNombreCompleto(Usuario usuario) {
        StringBuilder nombre = new StringBuilder();
        nombre.append(usuario.getNombre()).append(" ");
        nombre.append(usuario.getPrimerApellido());
        if (usuario.getSegundoApellido() != null && !usuario.getSegundoApellido().isEmpty()) {
            nombre.append(" ").append(usuario.getSegundoApellido());
        }
        return nombre.toString().trim();
    }

    public String formatearNumeroCuenta(UUID idCuenta) {
        String uuid = idCuenta.toString().replace("-", "");
        return uuid.substring(uuid.length() - 8).toUpperCase();
    }
}
