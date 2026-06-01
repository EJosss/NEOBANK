package com.neobank.service;

import com.neobank.model.Tarjeta;
import com.neobank.model.enums.EstadoTarjeta;
import com.neobank.repository.TarjetaRepository;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class TarjetaService {

    private final TarjetaRepository tarjetaRepository;

    public TarjetaService(TarjetaRepository tarjetaRepository) {
        this.tarjetaRepository = tarjetaRepository;
    }

    public List<Tarjeta> listarTodas() {
        return (List<Tarjeta>) tarjetaRepository.findAll();
    }

    public void guardar(Tarjeta tarjeta) {
        tarjetaRepository.save(tarjeta);
    }

    public void bloquear(Long id) {
        Tarjeta t = tarjetaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));
        t.setEstado(EstadoTarjeta.BLOQUEADA);
        tarjetaRepository.update(t);
    }
}