package com.neobank.model;

import com.neobank.model.enums.EstadoCuenta;
import com.neobank.model.enums.TipoCuenta;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@MappedEntity(
        value = "cuentas",
        namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class CuentaBancaria {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String numeroCuenta;

    private BigDecimal saldo = BigDecimal.ZERO;

    private TipoCuenta tipoCuenta;

    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    private Long idCliente;

    private LocalDate fechaApertura = LocalDate.now();

    // Cada tipo de cuenta implementa su propia lógica
    public abstract boolean permiteDescubierto();
    public abstract BigDecimal calcularInteres();
}