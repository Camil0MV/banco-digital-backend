package co.edu.udea.bancodigital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Configuración para habilitar auditoría en las entidades JPA.
 * Activa @CreatedDate y @LastModifiedDate automáticamente.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

	/**
	 * Bean que proporciona información del auditor (usuario actual).
	 * Por ahora retorna un auditor genérico.
	 */
	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of("SYSTEM");
	}
}
