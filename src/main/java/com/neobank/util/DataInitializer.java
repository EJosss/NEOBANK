package com.neobank.util;

import com.neobank.model.Usuario;
import com.neobank.model.enums.RolUsuario;
import com.neobank.repository.UsuarioRepository;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;

@Singleton
public class DataInitializer implements ApplicationEventListener<StartupEvent> {

    private final UsuarioRepository usuarioRepository;

    public DataInitializer(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador de Sistema");
            admin.setDni("00000000");
            admin.setTelefono("900000000");
            admin.setCorreo("admin@neobank.com");
            admin.setUsername("admin");
            admin.setPassword(PasswordUtils.encriptarSHA256("admin123"));
            admin.setRol(RolUsuario.ADMINISTRADOR); // 🚀 Asignación corregida a Enum
            admin.setActivo(true);
            usuarioRepository.save(admin);

            Usuario cajero = new Usuario();
            cajero.setNombre("Cajero Principal");
            cajero.setDni("11111111");
            cajero.setTelefono("911111111");
            cajero.setCorreo("cajero@neobank.com");
            cajero.setUsername("cajero");
            cajero.setPassword(PasswordUtils.encriptarSHA256("cajero123"));
            cajero.setRol(RolUsuario.CAJERO); // 🚀 Asignación corregida a Enum
            cajero.setActivo(true);
            usuarioRepository.save(cajero);

            System.out.println("====== [NEOBANK] Usuarios iniciales creados con éxito en el arranque ======");
        }
    }
}