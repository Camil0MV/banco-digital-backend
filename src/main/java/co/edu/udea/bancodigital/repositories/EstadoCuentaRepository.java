package co.edu.udea.bancodigital.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.udea.bancodigital.models.entities.catalogs.EstadoCuenta;

@Repository
public interface EstadoCuentaRepository extends JpaRepository<EstadoCuenta, Integer> {
    Optional<EstadoCuenta> findByNombreIgnoreCase(String nombre);
}
