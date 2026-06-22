package com.neobank.service;

import com.neobank.model.Tarjeta;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoTarjeta;
import com.neobank.model.enums.RolUsuario;
import com.neobank.model.enums.TipoTarjeta;
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

    public void guardar(Tarjeta tarjeta, Usuario usuarioActual) {
        if (tarjeta.getTipoTarjeta() == TipoTarjeta.CREDITO && usuarioActual != null && usuarioActual.getRol() == RolUsuario.CAJERO) {
            throw new IllegalArgumentException("⛔ ALERTA DE SEGURIDAD: Un cajero solo puede emitir tarjetas de DÉBITO. Crédito requiere Administrador.");
        }
        tarjetaRepository.save(tarjeta);
    }

    public void bloquear(Long id, Usuario usuarioActual) {
        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.CAJERO) {
            throw new IllegalArgumentException("⛔ ALERTA DE SEGURIDAD: Un cajero no tiene autorización para bloquear tarjetas.");
        }
        Tarjeta t = tarjetaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));
        t.setEstado(EstadoTarjeta.BLOQUEADA);
        tarjetaRepository.update(t);
    }
}