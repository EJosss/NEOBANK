package com.neobank.util;

import com.neobank.model.Usuario;
import com.neobank.model.enums.RolUsuario;
import com.neobank.repository.UsuarioRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;

@Singleton
@Requires(notEnv = "test")
public class DataInitializer
        implements ApplicationEventListener<StartupEvent> {

    private final UsuarioRepository usuarioRepository;

    public DataInitializer(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        // Solo inserta si no existen usuarios
        if (usuarioRepository.count() == 0) {

            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setDni("00000001");
            admin.setTelefono("999999999");
            admin.setCorreo("admin@neobank.com");
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRol(RolUsuario.ADMINISTRADOR);
            admin.setActivo(true);
            usuarioRepository.save(admin);

            Usuario cajero = new Usuario();
            cajero.setNombre("Carlos Cajero");
            cajero.setDni("00000002");
            cajero.setTelefono("988888888");
            cajero.setCorreo("cajero@neobank.com");
            cajero.setUsername("cajero");
            cajero.setPassword("cajero123");
            cajero.setRol(RolUsuario.CAJERO);
            cajero.setActivo(true);
            usuarioRepository.save(cajero);

            System.out.println("✅ Usuarios iniciales creados");
        }
    }
}