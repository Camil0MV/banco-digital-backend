package co.edu.udea.bancodigital.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.udea.bancodigital.models.entities.Cuenta;
import co.edu.udea.bancodigital.models.entities.Transaccion;

/**
 * Repositorio para la entidad Transaccion.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, UUID> {

	/**
	 * Obtiene todas las transacciones de una cuenta como origen.
	 *
	 * @param cuentaOrigen la cuenta origen a filtrar
	 * @return lista de transacciones donde la cuenta es el origen
	 */
	List<Transaccion> findAllByCuentaOrigen(Cuenta cuentaOrigen);

	/**
	 * Obtiene todas las transacciones donde una cuenta es origen o destino.
	 *
	 * @param cuentaOrigen la cuenta como origen
	 * @param cuentaDestino la cuenta como destino
	 * @return lista de transacciones relacionadas con la cuenta
	 */
	List<Transaccion> findAllByCuentaOrigenOrCuentaDestino(Cuenta cuentaOrigen, Cuenta cuentaDestino);

	/**
	 * Obtiene todas las transacciones de una cuenta en un rango de fechas.
	 *
	 * @param cuentaOrigen la cuenta origen a filtrar
	 * @param inicio la fecha de inicio del rango
	 * @param fin la fecha de fin del rango
	 * @return lista de transacciones en el rango de fechas especificado
	 */
	List<Transaccion> findAllByCuentaOrigenAndFechaBetween(Cuenta cuentaOrigen, LocalDate inicio, LocalDate fin);
}
