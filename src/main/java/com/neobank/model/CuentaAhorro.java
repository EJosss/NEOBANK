package com.neobank.model;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CuentaAhorro extends CuentaBancaria {

    private static final BigDecimal TASA_INTERES = new BigDecimal("0.035");

    @Override
    public boolean permiteDescubierto() {
        return false; // Ahorro NO permite saldo negativo
    }

    @Override
    public BigDecimal calcularInteres() {
        return getSaldo().multiply(TASA_INTERES);
    }
}