package com.neobank.model;

import com.neobank.model.enums.EstadoTarjeta;
import com.neobank.model.enums.TipoTarjeta;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.naming.NamingStrategies;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@MappedEntity(value = "tarjeta", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase.class)
@Data
@NoArgsConstructor
public class Tarjeta {
    @Id
    @GeneratedValue(GeneratedValue.Type.AUTO)
    private Long id;

    private String numeroTarjeta;
    private Long idCuenta;
    private TipoTarjeta tipoTarjeta;
    private BigDecimal limite;
    private String fechaVencimiento;
    private String ccv;
    private EstadoTarjeta estado = EstadoTarjeta.ACTIVA;
}