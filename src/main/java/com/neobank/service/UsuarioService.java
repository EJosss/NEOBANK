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
        // Se eliminó el backdoor hardcodeado. Ahora todo pasa por la Base de Datos.
        String passwordEncriptada = PasswordUtils.encriptarSHA256(password);

        return usuarioRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(passwordEncriptada) && u.isActivo());
    }

    public Usuario guardar(Usuario usuario, String passwordSegura) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con username: " + usuario.getUsername());
        }
        usuario.setPassword(PasswordUtils.encriptarSHA256(passwordSegura));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}