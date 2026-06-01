package com.neobank.model;

import com.neobank.model.enums.RolUsuario;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import lombok.*;

@MappedEntity(
        value = "usuario",
        namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String nombre;
    private String dni;
    private String telefono;
    private String correo;
    private String username;
    private String password;
    private RolUsuario rol;

    // CORRECCIÓN: boolean con "b" minúscula para que Lombok genere isActivo()
    private boolean activo = true;
}