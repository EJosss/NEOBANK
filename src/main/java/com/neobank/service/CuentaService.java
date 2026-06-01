package com.neobank.service;

import com.neobank.model.CuentaBancaria;
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

    public void eliminar(Long id) {
        cuentaRepository.deleteById(id);
    }
}