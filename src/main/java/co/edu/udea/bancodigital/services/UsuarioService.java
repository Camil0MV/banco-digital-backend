package co.edu.udea.bancodigital.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.udea.bancodigital.dtos.requests.ActualizarDatosRequest;
import co.edu.udea.bancodigital.dtos.requests.RegistroRequest;
import co.edu.udea.bancodigital.dtos.responses.ActualizarDatosResponse;
import co.edu.udea.bancodigital.dtos.responses.RegistroResponse;
import co.edu.udea.bancodigital.exception.EntityNotFoundException;
import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.catalogs.Rol;
import co.edu.udea.bancodigital.models.entities.catalogs.TipoDocumento;
import co.edu.udea.bancodigital.models.pks.UsuarioId;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String ROL_CLIENTE = "CLIENTE";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public RegistroResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + request.getCorreo());
        }

        Integer idTipoDoc = request.getIdTipoDoc();
        UsuarioId id = new UsuarioId(idTipoDoc, request.getNumeroDocumento());

        if (usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese documento");
        }

        TipoDocumento tipoDocumento = entityManager.find(TipoDocumento.class, idTipoDoc);
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("No existe tipo_documento con id: " + idTipoDoc);
        }

        Rol rolCliente = findRolCliente();

        Usuario usuario = Usuario.builder()
                .id(id)
                .tipoDocumento(tipoDocumento)
                .nombre(request.getNombre())
                .primerApellido(request.getPrimerApellido())
                .segundoApellido(request.getSegundoApellido())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .rol(rolCliente)
                .build();

        usuarioRepository.save(usuario);

        return RegistroResponse.builder()
            .idTipoDoc(tipoDocumento.getId())
            .tipoDocumento(tipoDocumento.getNombre())
                .numeroDocumento(maskNumeroDocumento(id.getNumeroDocumento()))
                .nombre(usuario.getNombre())
                .primerApellido(usuario.getPrimerApellido())
                .segundoApellido(usuario.getSegundoApellido())
                .correo(usuario.getCorreo())
            .idRol(rolCliente.getId())
            .rol(rolCliente.getNombre())
                .build();
    }

    public ActualizarDatosResponse actualizarMisDatos(ActualizarDatosRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        String correoActual = authentication.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correoActual)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));

        String nuevoCorreo = request.getCorreo().trim().toLowerCase();
        if (!usuario.getCorreo().equalsIgnoreCase(nuevoCorreo) && usuarioRepository.existsByCorreo(nuevoCorreo)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + nuevoCorreo);
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setPrimerApellido(request.getPrimerApellido().trim());
        usuario.setSegundoApellido(request.getSegundoApellido().trim());
        usuario.setDireccion(request.getDireccion().trim());
        usuario.setTelefono(request.getTelefono().trim());
        usuario.setCorreo(nuevoCorreo);

        Usuario actualizado = usuarioRepository.save(usuario);

        return ActualizarDatosResponse.builder()
                .idTipoDoc(actualizado.getTipoDocumento().getId())
                .tipoDocumento(actualizado.getTipoDocumento().getNombre())
                .numeroDocumento(maskNumeroDocumento(actualizado.getId().getNumeroDocumento()))
                .nombre(actualizado.getNombre())
                .primerApellido(actualizado.getPrimerApellido())
                .segundoApellido(actualizado.getSegundoApellido())
                .direccion(actualizado.getDireccion())
                .telefono(actualizado.getTelefono())
                .correo(actualizado.getCorreo())
                .idRol(actualizado.getRol().getId())
                .rol(actualizado.getRol().getNombre())
                .updatedAt(actualizado.getUpdatedAt())
                .build();
    }

    private String maskNumeroDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            return "****";
        }

        int visible = Math.min(4, numeroDocumento.length());
        String tail = numeroDocumento.substring(numeroDocumento.length() - visible);
        return "****" + tail;
    }

    private Rol findRolCliente() {
        return entityManager.createQuery(
                        "select r from Rol r where upper(r.nombre) = :nombre", Rol.class)
                .setParameter("nombre", ROL_CLIENTE)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new DataIntegrityViolationException(
                        "No existe el rol CLIENTE en la tabla roles"));
    }
}
