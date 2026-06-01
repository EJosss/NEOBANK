package com.neobank.model;

import com.neobank.model.enums.EstadoCliente;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import lombok.*;

@MappedEntity(
        value = "cliente",
        namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String nombre;
    private String dni;
    private String telefono;
    private String correo;
    private String direccion;

    @Builder.Default
    private EstadoCliente estado = EstadoCliente.ACTIVO;
}