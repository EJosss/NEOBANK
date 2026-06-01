package com.neobank.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Persona {
    protected String nombre;
    protected String dni;
    protected String telefono;
    protected String correo;
}