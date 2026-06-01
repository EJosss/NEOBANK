package com.neobank.service;

import com.neobank.model.Usuario;
import com.neobank.repository.UsuarioRepository;
import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Login — valida usuario y contraseña
    public Optional<Usuario> login(String username, String password) {
        return usuarioRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .filter(Usuario::isActivo);
    }

    public Usuario guardar(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con username: " + usuario.getUsername()
            );
        }
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}