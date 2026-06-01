package com.neobank.repository;

import com.neobank.model.CuentaBancaria;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

@JdbcRepository(dialect = Dialect.H2)
public interface CuentaRepository extends CrudRepository<CuentaBancaria, Long> {
}