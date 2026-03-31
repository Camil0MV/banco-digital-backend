package co.edu.udea.bancodigital.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.udea.bancodigital.models.entities.Usuario;
import co.edu.udea.bancodigital.models.entities.UsuarioId;
import co.edu.udea.bancodigital.models.enums.Rol;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UsuarioId> {

	/**
	 * Busca un usuario por su correo electrónico.
	 *
	 * @param correo el correo a buscar
	 * @return Optional con el usuario si existe
	 */
	Optional<Usuario> findByCorreo(String correo);

	/**
	 * Verifica si existe un usuario con el correo especificado.
	 *
	 * @param correo el correo a verificar
	 * @return true si existe, false en caso contrario
	 */
	boolean existsByCorreo(String correo);

	/**
	 * Obtiene todos los usuarios con un rol específico.
	 *
	 * @param rol el rol a filtrar
	 * @return lista de usuarios con el rol especificado
	 */
	List<Usuario> findAllByRol(Rol rol);
}
