package com.neobank.model;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CuentaCorriente extends CuentaBancaria {

    private static final BigDecimal LIMITE_DESCUBIERTO = new BigDecimal("500.00");

}