package co.edu.udea.bancodigital.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import co.edu.udea.bancodigital.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;

/**
 * Configuración para cargar detalles de usuario desde la base de datos.
 */
@Configuration
@RequiredArgsConstructor
public class UserDetailsServiceConfig {

	private final UsuarioRepository usuarioRepository;

	/**
	 * Bean que proporciona el servicio de detalles de usuario.
	 * Busca usuarios por su numero de documento en la base de datos.
	 */
	@Bean
	public UserDetailsService userDetailsService() {
		return numeroDocumento -> usuarioRepository
				.findByCorreo(numeroDocumento)
				.map(UserDetailsMapper::mapToUserDetails)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + numeroDocumento));
	}
}
