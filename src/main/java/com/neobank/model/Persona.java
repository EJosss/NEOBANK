package com.neobank.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Persona {

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 números.")
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 números.")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo electrónico válido.")
    private String correo;
}