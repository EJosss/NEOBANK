package com.neobank.repository;

import com.neobank.model.Usuario;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    // 🚀 CAMBIADO: Ahora coinciden exactamente con la variable "username"
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);

}