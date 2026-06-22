package com.neobank.repository;

import com.neobank.model.CuentaBancaria;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface CuentaRepository extends CrudRepository<CuentaBancaria, Long> {

    Optional<CuentaBancaria> findByNumeroCuenta(String numeroCuenta);
    List<CuentaBancaria> findByIdCliente(Long idCliente);
    boolean existsByNumeroCuenta(String numeroCuenta);

}