package com.neobank.model;

import com.neobank.model.enums.EstadoCliente;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedEntity(
        value = "cliente",
        namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Cliente extends Persona {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    @NotBlank(message = "La dirección es obligatoria.")
    private String direccion;

    @Builder.Default
    private EstadoCliente estado = EstadoCliente.ACTIVO;
}