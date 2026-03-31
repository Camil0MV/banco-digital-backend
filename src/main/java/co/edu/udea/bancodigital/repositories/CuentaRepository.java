package co.edu.udea.bancodigital.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Usuario;

/**
 * Repositorio para la entidad Cuenta.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, UUID> {

	/**
	 * Obtiene todas las cuentas de un usuario específico.
	 *
	 * @param dueno el usuario dueño de las cuentas
	 * @return lista de cuentas del usuario
	 */
	List<Cuenta> findAllByDueno(Usuario dueno);

	/**
	 * Verifica si un usuario tiene al menos una cuenta.
	 *
	 * @param dueno el usuario a verificar
	 * @return true si tiene cuentas, false en caso contrario
	 */
	boolean existsByDueno(Usuario dueno);
}
