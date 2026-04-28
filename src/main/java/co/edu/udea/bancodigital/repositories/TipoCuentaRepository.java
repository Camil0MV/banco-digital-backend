package co.edu.udea.bancodigital.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.udea.bancodigital.models.entities.catalogs.TipoCuenta;

@Repository
public interface TipoCuentaRepository extends JpaRepository<TipoCuenta, Integer> {
}
