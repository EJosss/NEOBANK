package com.neobank.model;

import com.neobank.model.enums.TipoMovimiento;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedEntity(
        value = "movimiento",
        namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class
)
@Data
@NoArgsConstructor
public class Movimiento {
    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private Long idCuenta;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal monto;
    private LocalDateTime fecha = LocalDateTime.now();
    private String descripcion;
}