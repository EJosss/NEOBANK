package com.neobank.service;

import com.neobank.model.Factura;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoFactura;
import com.neobank.model.enums.RolUsuario;
import com.neobank.repository.FacturaRepository;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Singleton
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public Factura registrarFactura(Long idCliente, String concepto, BigDecimal montoBase) {
        if (montoBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto base de la factura debe ser mayor a S/ 0.00.");
        }

        BigDecimal subtotal = montoBase.setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        // EXTRAEMOS EL ID DE FORMA 100% SEGURA EN JAVA
        // Buscamos la última factura. Si existe, extraemos su ID. Si no, usamos 0.
        long ultimoId = facturaRepository.findFirstOrderByIdDesc()
                .map(Factura::getId)
                .orElse(0L);

        String numeroCorrelativo = String.format("E001-%06d", ultimoId + 1);

        Factura nuevaFactura = Factura.builder()
                .numeroFactura(numeroCorrelativo)
                .idCliente(idCliente)
                .concepto(concepto)
                .fechaEmision(LocalDateTime.now())
                .subtotal(subtotal)
                .igv(igv)
                .total(total)
                .estado(EstadoFactura.PENDIENTE)
                .build();

        return facturaRepository.save(nuevaFactura);
    }

    public List<Factura> listarTodas() { return (List<Factura>) facturaRepository.findAll(); }
    public Optional<Factura> buscarPorNumero(String numero) { return facturaRepository.findByNumeroFactura(numero); }

    public void anularFactura(Long idFactura, Usuario usuarioActual) {
        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.CAJERO) {
            throw new IllegalArgumentException("⛔ ALERTA DE SEGURIDAD: Un cajero no tiene autorización para anular comprobantes contables.");
        }

        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new IllegalArgumentException("La factura especificada no existe."));

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalArgumentException("Esta factura ya se encuentra anulada.");
        }

        factura.setEstado(EstadoFactura.ANULADA);
        facturaRepository.update(factura);
    }

    public void pagarFactura(Long idFactura) {
        facturaRepository.findById(idFactura).ifPresent(f -> {
            f.setEstado(EstadoFactura.PAGADA);
            facturaRepository.update(f);
        });
    }
}