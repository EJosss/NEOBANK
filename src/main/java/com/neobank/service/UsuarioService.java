package com.neobank.service;

import com.neobank.model.Usuario;
import com.neobank.repository.UsuarioRepository;
import com.neobank.util.PasswordUtils;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> login(String username, String password) {
        // 1. Buscamos al usuario únicamente por su username
        return usuarioRepository.findByUsername(username)
                // 2. Filtramos: el usuario debe estar activo Y la validación BCrypt debe ser exitosa
                .filter(u -> u.isActivo() && PasswordUtils.verificarPassword(password, u.getPassword()));
    }

    public Usuario guardar(Usuario usuario, String passwordSegura) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con username: " + usuario.getUsername());
        }
        // Encriptamos la contraseña plana usando BCrypt antes de enviarla a la base de datos
        usuario.setPassword(PasswordUtils.encriptarBCrypt(passwordSegura));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}