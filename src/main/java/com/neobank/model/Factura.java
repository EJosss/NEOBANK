package com.neobank.model;

import com.neobank.model.enums.EstadoFactura;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedEntity(
        value = "facturas",
        namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String numeroFactura;

    private Long idCliente;

    private String concepto;

    private LocalDateTime fechaEmision;

    private BigDecimal subtotal;

    private BigDecimal igv;

    private BigDecimal total;

    @Builder.Default
    private EstadoFactura estado = EstadoFactura.PENDIENTE;
}