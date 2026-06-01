package com.neobank.service;

import com.neobank.model.CuentaBancaria;
import com.neobank.model.Movimiento;
import com.neobank.model.enums.TipoMovimiento;
import com.neobank.repository.CuentaRepository;
import com.neobank.repository.MovimientoRepository;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    public MovimientoService(MovimientoRepository movimientoRepository, CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public List<Movimiento> listarPorCuenta(Long idCuenta) {
        return movimientoRepository.findByIdCuenta(idCuenta);
    }

    public void registrar(Long idCuenta, TipoMovimiento tipo, BigDecimal monto, String descripcion) {
        CuentaBancaria cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));

        if (tipo == TipoMovimiento.RETIRO && cuenta.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        if (tipo == TipoMovimiento.DEPOSITO) {
            cuenta.setSaldo(cuenta.getSaldo().add(monto));
        } else if (tipo == TipoMovimiento.RETIRO) {
            cuenta.setSaldo(cuenta.getSaldo().subtract(monto));
        }
        cuentaRepository.update(cuenta);

        Movimiento mov = new Movimiento();
        mov.setIdCuenta(idCuenta);
        mov.setTipoMovimiento(tipo);
        mov.setMonto(monto);
        mov.setDescripcion(descripcion);
        mov.setFecha(LocalDateTime.now());
        movimientoRepository.save(mov);
    }

    // 🚀 AQUÍ ESTÁ EL MÉTODO QUE FALTABA
    public void transferir(Long idCuentaOrigen, String numeroCuentaDestino, BigDecimal monto, String descripcion) {
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0.");
        }

        CuentaBancaria origen = cuentaRepository.findById(idCuentaOrigen)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta de origen no encontrada."));

        if (origen.getNumeroCuenta().equals(numeroCuentaDestino)) {
            throw new IllegalArgumentException("No puedes transferirte a ti mismo.");
        }

        if (origen.getSaldo().compareTo(monto) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para transferir.");
        }

        CuentaBancaria destino = null;
        for (CuentaBancaria c : cuentaRepository.findAll()) {
            if (c.getNumeroCuenta().equals(numeroCuentaDestino)) {
                destino = c;
                break;
            }
        }

        if (destino == null) {
            throw new IllegalArgumentException("La cuenta de destino no existe.");
        }

        registrar(origen.getId(), TipoMovimiento.RETIRO, monto, "Transferencia a " + numeroCuentaDestino + " - " + descripcion);
        registrar(destino.getId(), TipoMovimiento.DEPOSITO, monto, "Transferencia de " + origen.getNumeroCuenta() + " - " + descripcion);
    }
}