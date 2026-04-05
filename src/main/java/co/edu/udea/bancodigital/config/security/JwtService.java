package co.edu.udea.bancodigital.config.security;

import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.edu.udea.bancodigital.models.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para generar, validar y extraer información de JWT.
 * Utiliza la librería jjwt para manejar tokens JWT.
 */
@Service
@Slf4j
public class JwtService {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration:86400000}")
	private long jwtExpiration;

	/**
	 * Genera un JWT token para un usuario.
	 *
	 * @param usuario el usuario para el cual generar el token
	 * @return el token JWT
	 */
	public String generateToken(Usuario usuario) {
		log.debug("Generando JWT token para usuario: {}", usuario.getCorreo());
		String rolNombre = usuario.getRol().getNombre().toUpperCase(Locale.ROOT);

		return Jwts.builder()
				.subject(usuario.getCorreo())
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtExpiration))
				.signWith(getSigningKey())
				.claim("rol", rolNombre)
				.compact();
	}

	/**
	 * Valida si un JWT token es válido.
	 *
	 * @param token el token a validar
	 * @return true si el token es válido, false en caso contrario
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token);
			log.debug("Token JWT validado exitosamente");
			return true;
		} catch (Exception ex) {
			log.warn("Token JWT inválido: {}", ex.getMessage());
			return false;
		}
	}

	/**
	 * Extrae el correo (subject) del JWT token.
	 *
	 * @param token el token
	 * @return el correo del usuario
	 */
	public String extractCorreo(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrae un claim específico del JWT token.
	 *
	 * @param <T>            el tipo del claim
	 * @param token          el token
	 * @param claimsResolver la función para resolver el claim
	 * @return el valor del claim
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Extrae el rol del JWT token.
	 *
	 * @param token el token
	 * @return el rol del usuario
	 */
	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("rol", String.class));
	}

	/**
	 * Extrae el correo del claim customizado del JWT.
	 *
	 * @param token el token
	 * @return el correo del usuario
	 */
	public String extractEmailFromClaim(String token) {
		return extractCorreo(token);
	}

	/**
	 * Extrae todos los claims del JWT token.
	 *
	 * @param token el token
	 * @return los claims
	 */
	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/**
	 * Obtiene la clave de firma para el JWT.
	 *
	 * @return la clave de firma
	 */
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}
}
