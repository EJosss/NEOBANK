package com.neobank.model;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CuentaAhorro extends CuentaBancaria {

    private static final BigDecimal TASA_INTERES = new BigDecimal("0.035");

}