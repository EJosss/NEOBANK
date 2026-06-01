package com.neobank.repository;

import com.neobank.model.Movimiento;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface MovimientoRepository extends CrudRepository<Movimiento, Long> {
    List<Movimiento> findByIdCuenta(Long idCuenta);
}