package co.edu.udea.bancodigital.usuario;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.UsuarioId;
import co.edu.udea.bancodigital.models.enums.Rol;
import co.edu.udea.bancodigital.repositories.UsuarioRepository;
import co.edu.udea.bancodigital.usuario.dto.RegistroRequest;
import co.edu.udea.bancodigital.usuario.dto.RegistroResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + request.getCorreo());
        }

        UsuarioId id = new UsuarioId(request.getTipoDocumento(), request.getNumeroDocumento());

        if (usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese documento");
        }

        Usuario usuario = Usuario.builder()
                .id(id)
                .nombre(request.getNombre())
                .primerApellido(request.getPrimerApellido())
                .segundoApellido(request.getSegundoApellido())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .rol(Rol.CLIENTE)
                .fechaRegistro(LocalDateTime.now())
                .build();

        usuarioRepository.save(usuario);

        return RegistroResponse.builder()
                .tipoDocumento(id.getTipoDocumento())
                .numeroDocumento(id.getNumeroDocumento())
                .nombre(usuario.getNombre())
                .primerApellido(usuario.getPrimerApellido())
                .segundoApellido(usuario.getSegundoApellido())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .build();
    }
}
