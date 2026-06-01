package com.neobank.repository;

import com.neobank.model.Tarjeta;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface TarjetaRepository extends CrudRepository<Tarjeta, Long> {
    List<Tarjeta> findByIdCuenta(Long idCuenta);
}