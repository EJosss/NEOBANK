package com.neobank.model;

import com.neobank.model.enums.RolUsuario;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedEntity(
        value = "usuario",
        namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario extends Persona {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String username;
    private String password;

    // 🚀 Ahora sí usamos tu Enum para mayor seguridad en los tipos
    private RolUsuario rol;

    private boolean activo;
}