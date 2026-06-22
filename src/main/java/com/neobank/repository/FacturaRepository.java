package com.neobank.repository;

import com.neobank.model.Factura;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface FacturaRepository extends CrudRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    boolean existsByNumeroFactura(String numeroFactura);

    // Micronaut creará la consulta SQL automáticamente en segundo plano.
    // Adiós al texto en rojo de IntelliJ.
    Optional<Factura> findFirstOrderByIdDesc();
}