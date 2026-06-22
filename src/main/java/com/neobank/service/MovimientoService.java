package com.neobank.service;

import com.neobank.model.CuentaBancaria;
import com.neobank.model.Movimiento;
import com.neobank.model.Factura;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoCuenta;
import com.neobank.model.enums.TipoMovimiento;
import com.neobank.repository.CuentaRepository;
import com.neobank.repository.MovimientoRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final FacturaService facturaService;

    // Inyección de dependencias por constructor
    public MovimientoService(MovimientoRepository movimientoRepository,
                             CuentaRepository cuentaRepository,
                             FacturaService facturaService) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
        this.facturaService = facturaService;
    }

    public List<Movimiento> listarTodos() {
        // 🚀 Casting redundante eliminado
        return movimientoRepository.findAll();
    }

    public List<Movimiento> listarPorCuenta(Long idCuenta) {
        return movimientoRepository.findByIdCuentaOrderByFechaDesc(idCuenta);
    }

    /**
     * 🚀 REGISTRO AUTOMÁTICO DE DEPÓSITOS Y RETIROS EN VENTANILLA
     */
    @Transactional
    public Movimiento registrar(Long idCuenta, TipoMovimiento tipo, BigDecimal monto, String descripcion, Usuario usuarioActual) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la operación debe ser estrictamente superior a S/ 0.00.");
        }

        // 1. Validar existencia de la cuenta
        CuentaBancaria cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta bancaria seleccionada no existe en el sistema."));

        // 2. Validar que la cuenta no esté dada de baja
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalArgumentException("Operación denegada: La cuenta bancaria se encuentra CERRADA.");
        }

        // 3. Modificar saldo según la naturaleza jurídica del movimiento
        if (tipo == TipoMovimiento.RETIRO) {
            if (cuenta.getSaldo().compareTo(monto) < 0) {
                throw new IllegalArgumentException("Fondos insuficientes en cuenta para efectuar el retiro solicitado.");
            }
            cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        } else if (tipo == TipoMovimiento.DEPOSITO) {
            cuenta.setSaldo(cuenta.getSaldo().add(monto));
        }

        // 4. Persistir el nuevo estado financiero de la cuenta
        cuentaRepository.update(cuenta);

        // 5. Construir y guardar el registro (Respetando tu Builder original)
        Movimiento nuevoMovimiento = Movimiento.builder()
                .idCuenta(idCuenta)
                .tipoMovimiento(tipo)
                .monto(monto)
                .descripcion(descripcion.trim().isEmpty() ? "Operación en Ventanilla" : descripcion.trim())
                .fecha(LocalDateTime.now())
                .build();

        Movimiento movimientoGuardado = movimientoRepository.save(nuevoMovimiento);

        // 6. 🧾 EMISIÓN Y COBRO AUTOMÁTICO DE COMPROBANTE ELECTRÓNICO (FACTURA)
        String conceptoOficial = (tipo == TipoMovimiento.DEPOSITO)
                ? "Depósito de Efectivo en Ventanilla"
                : "Retiro de Efectivo en Ventanilla";

        Factura fact = facturaService.registrarFactura(cuenta.getIdCliente(), conceptoOficial, monto);

        // 🚀 Error de tipeo corregido
        facturaService.pagarFactura(fact.getId());

        return movimientoGuardado;
    }

    /**
     * 🚀 REGISTRO AUTOMÁTICO DE TRANSFERENCIAS ENTRE CUENTAS INTERBANCARIAS
     */
    @Transactional
    public void transferir(Long idCuentaOrigen, String numeroCuentaDestino, BigDecimal monto, String descripcion, Usuario usuarioActual) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a transferir debe ser estrictamente superior a S/ 0.00.");
        }

        // 1. Validar existencia de la cuenta emisora
        CuentaBancaria origen = cuentaRepository.findById(idCuentaOrigen)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta de origen especificada no existe."));

        // 2. Buscar la cuenta receptora por su número correlativo de cuenta
        CuentaBancaria destino = cuentaRepository.findByNumeroCuenta(numeroCuentaDestino)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta de destino '" + numeroCuentaDestino + "' no está registrada en NeoBank."));

        // 3. Impedir auto-transferencias redundantes
        if (origen.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("Operación inválida: No es posible transferir fondos hacia la misma cuenta de origen.");
        }

        // 4. Validar estados operativos activos de ambas cuentas
        if (origen.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalArgumentException("La cuenta de origen se encuentra CERRADA.");
        }
        if (destino.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalArgumentException("La cuenta de destino se encuentra CERRADA.");
        }

        // 5. Validar capacidad de cobertura del emisor
        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente en cuenta de origen para procesar la transferencia.");
        }

        // 6. Ejecución contable de saldos cruzados
        origen.setSaldo(origen.getSaldo().subtract(monto));
        destino.setSaldo(destino.getSaldo().add(monto));

        cuentaRepository.update(origen);
        cuentaRepository.update(destino);

        String descBase = descripcion.trim().isEmpty() ? "Transferencia de fondos" : descripcion.trim();

        // 7. Generar auditoría de salida de dinero (Retiro) en la cuenta de origen
        Movimiento movOrigen = Movimiento.builder()
                .idCuenta(origen.getId())
                .tipoMovimiento(TipoMovimiento.RETIRO)
                .monto(monto)
                .descripcion("Envío a Cuenta N° " + destino.getNumeroCuenta() + " | " + descBase)
                .fecha(LocalDateTime.now())
                .build();
        movimientoRepository.save(movOrigen);

        // 8. Generar auditoría de entrada de dinero (Depósito) en la cuenta de destino
        Movimiento movDestino = Movimiento.builder()
                .idCuenta(destino.getId())
                .tipoMovimiento(TipoMovimiento.DEPOSITO)
                .monto(monto)
                .descripcion("Abono desde Cuenta N° " + origen.getNumeroCuenta() + " | " + descBase)
                .fecha(LocalDateTime.now())
                .build();
        movimientoRepository.save(movDestino);

        // 9. 🧾 EMISIÓN DE COMPROBANTES DE TRANSFERENCIA AUTOMÁTICOS PARA AMBOS TITULARES
        Factura factEmisor = facturaService.registrarFactura(origen.getIdCliente(), "Transferencia de Fondos (Cargo Enviado)", monto);
        facturaService.pagarFactura(factEmisor.getId());

        Factura factReceptor = facturaService.registrarFactura(destino.getIdCliente(), "Transferencia de Fondos (Abono Recibido)", monto);
        facturaService.pagarFactura(factReceptor.getId());
    }
}