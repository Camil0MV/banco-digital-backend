package co.edu.udea.bancodigital.config.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro JWT que se ejecuta una vez por solicitud.
 * Extrae y valida el token JWT del header Authorization.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

			if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
				log.debug("No JWT token found in Authorization header");
				filterChain.doFilter(request, response);
				return;
			}

			// Extrae el token del header Authorization
			final String jwt = authHeader.substring(BEARER_PREFIX.length());

			// Valida el token
			if (jwtService.validateToken(jwt)) {
				final String numeroDocumento = jwtService.extractCorreo(jwt);
				log.debug("JWT validado para usuario: {}", numeroDocumento);

				// Carga los detalles del usuario
				final UserDetails userDetails = userDetailsService.loadUserByUsername(numeroDocumento);

				// Crea un token de autenticación
				final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// Establece la autenticación en el SecurityContext
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
				log.debug("Autenticación establecida para usuario: {}", numeroDocumento);
			} else {
				log.warn("JWT token inválido para la solicitud: {}", request.getRequestURI());
			}

		} catch (Exception ex) {
			log.error("Error procesando JWT token: {}", ex.getMessage(), ex);
		}

		filterChain.doFilter(request, response);
	}
}
