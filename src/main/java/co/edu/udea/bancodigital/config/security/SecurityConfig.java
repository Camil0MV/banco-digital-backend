package co.edu.udea.bancodigital.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configuración global de seguridad para la API REST.
 * Define el filtro de cadena de seguridad, manejo de sesiones y autenticación JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	/**
	 * Bean para codificar contraseñas.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Bean para el proveedor de autenticación.
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	/**
	 * Bean para el AuthenticationManager.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Bean para el filtro JWT.
	 */
	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtService, userDetailsService);
	}

	/**
	 * Configura la cadena de filtros de seguridad.
	 * Define las rutas públicas, protegidas y de administrador.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Deshabilita CSRF (no es necesario para REST API con tokens JWT)
				.csrf(csrf -> csrf.disable())
				// Deshabilita CORS (se configurará posteriormente)
				.cors(cors -> cors.disable())
				// Configura el manejo de sesiones como STATELESS (sin sesiones)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Configura las autorizaciones
				.authorizeHttpRequests(authz -> authz
						// Rutas públicas - no requieren autenticación
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro").permitAll()
						// Health checks para Render/Actuator
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						// Swagger UI
						.requestMatchers("/swagger", "/swagger/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
						.permitAll()
						// Rutas de administrador - requieren rol ADMIN
						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
						// El resto de rutas requieren autenticación
						.anyRequest().authenticated())
				// Agrega el filtro JWT antes del filtro de Usuario-Contraseña
				.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				// Configura el proveedor de autenticación
				.authenticationProvider(authenticationProvider());

		return http.build();
	}
}
