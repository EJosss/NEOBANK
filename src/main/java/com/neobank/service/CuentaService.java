package com.neobank.service;

import com.neobank.model.CuentaBancaria;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoCuenta;
import com.neobank.model.enums.RolUsuario;
import com.neobank.repository.CuentaRepository;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public List<CuentaBancaria> listarTodas() {
        return (List<CuentaBancaria>) cuentaRepository.findAll();
    }

    public CuentaBancaria guardar(CuentaBancaria cuenta) {
        return cuentaRepository.save(cuenta);
    }

    // 🚀 OPCIÓN 2: Eliminación Lógica (Soft Delete)
    public void eliminar(Long id, Usuario usuarioActual) {
        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.CAJERO) {
            throw new IllegalArgumentException("⛔ ALERTA DE SEGURIDAD: Un cajero no tiene permisos para eliminar cuentas bancarias.");
        }

        // En lugar de borrar, marcamos la cuenta como CERRADA
        CuentaBancaria cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        cuenta.setEstado(EstadoCuenta.CERRADA); // 👈 AQUÍ ESTABA EL ERROR, YA ESTÁ CORREGIDO
        cuentaRepository.update(cuenta);
    }
}