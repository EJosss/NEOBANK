package com.neobank.repository;

import com.neobank.model.Cliente;
import com.neobank.model.enums.EstadoCliente;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface ClienteRepository extends CrudRepository<Cliente, Long> {

    Optional<Cliente> findByDni(String dni);

    List<Cliente> findByEstado(EstadoCliente estado);

    boolean existsByDni(String dni);

    List<Cliente> findByNombreContainsIgnoreCase(String nombre);
}