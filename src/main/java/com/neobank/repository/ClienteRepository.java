package com.neobank.repository;

import com.neobank.model.Cliente;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface ClienteRepository extends CrudRepository<Cliente, Long> {

    // Método para validar si un DNI ya existe
    boolean existsByDni(String dni);

    // Método para buscar un cliente exacto por su DNI
    Optional<Cliente> findByDni(String dni);

    // Método para el buscador por nombre (coincidencias parciales ignorando mayúsculas)
    List<Cliente> findByNombreContainsIgnoreCase(String nombre);
}