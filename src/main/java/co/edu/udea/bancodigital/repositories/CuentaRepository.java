package co.edu.udea.bancodigital.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	 * @param correo el usuario dueño de las cuentas
	 * @return lista de cuentas del usuario
	 */
	@Query("""
	    SELECT c FROM Cuenta c
	    JOIN FETCH c.tipoCuenta tc
	    JOIN FETCH c.estadoCuenta ec
	    JOIN FETCH c.dueno u
	    WHERE u.correo = :correo
	    """)
	List<Cuenta> findAllByDuenoCorreo(@Param("correo") String correo);


	@Query("""
	    SELECT c FROM Cuenta c
	    JOIN FETCH c.dueno u
	    JOIN FETCH c.estadoCuenta ec
	    WHERE c.idCuenta = :idCuenta
	    """)
	Optional<Cuenta> findByIdCuentaConDueno(@Param("idCuenta") UUID idCuenta);

	/**
	 * Verifica si un usuario tiene al menos una cuenta.
	 *
	 * @param dueno el usuario a verificar
	 * @return true si tiene cuentas, false en caso contrario
	 */
	boolean existsByDueno(Usuario dueno);
}
