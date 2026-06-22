package com.neobank.service;

import com.neobank.model.Cliente;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoCliente;
import com.neobank.model.enums.RolUsuario;
import com.neobank.repository.ClienteRepository;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente guardar(Cliente cliente) {
        List<Cliente> todos = (List<Cliente>) clienteRepository.findAll();

        // 1. Validar DNI único
        if (clienteRepository.existsByDni(cliente.getDni())) {
            throw new IllegalArgumentException("El número de DNI '" + cliente.getDni() + "' ya se encuentra registrado.");
        }

        // 2. Validar Nombre único
        boolean existeNombre = todos.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(cliente.getNombre()));
        if (existeNombre) {
            throw new IllegalArgumentException("El nombre '" + cliente.getNombre() + "' ya se encuentra registrado.");
        }

        // 3. Validar Correo único
        if (cliente.getCorreo() != null && !cliente.getCorreo().trim().isEmpty()) {
            boolean existeCorreo = todos.stream()
                    .anyMatch(c -> c.getCorreo() != null && c.getCorreo().equalsIgnoreCase(cliente.getCorreo().trim()));
            if (existeCorreo) {
                throw new IllegalArgumentException("El correo electrónico '" + cliente.getCorreo() + "' ya se encuentra registrado.");
            }
        }

        // 4. Validar Teléfono único
        if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
            boolean existeTelefono = todos.stream()
                    .anyMatch(c -> c.getTelefono() != null && c.getTelefono().trim().equals(cliente.getTelefono().trim()));
            if (existeTelefono) {
                throw new IllegalArgumentException("El número de teléfono '" + cliente.getTelefono() + "' ya se encuentra registrado.");
            }
        }

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() { return (List<Cliente>) clienteRepository.findAll(); }
    public Optional<Cliente> buscarPorId(Long id) { return clienteRepository.findById(id); }
    public Optional<Cliente> buscarPorDni(String dni) { return clienteRepository.findByDni(dni); }
    public List<Cliente> buscarPorNombre(String nombre) { return clienteRepository.findByNombreContainsIgnoreCase(nombre); }

    public Cliente actualizar(Cliente cliente) {
        List<Cliente> todos = (List<Cliente>) clienteRepository.findAll();

        // 1. Validar DNI único al editar
        boolean existeDni = todos.stream()
                .anyMatch(c -> c.getDni().equals(cliente.getDni()) && !c.getId().equals(cliente.getId()));
        if (existeDni) {
            throw new IllegalArgumentException("El número de DNI '" + cliente.getDni() + "' ya le pertenece a otro cliente.");
        }

        // 2. Validar Nombre único al editar
        boolean existeNombre = todos.stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(cliente.getNombre()) && !c.getId().equals(cliente.getId()));
        if (existeNombre) {
            throw new IllegalArgumentException("El nombre '" + cliente.getNombre() + "' ya le pertenece a otro cliente.");
        }

        // 3. Validar Correo único al editar
        if (cliente.getCorreo() != null && !cliente.getCorreo().trim().isEmpty()) {
            boolean existeCorreo = todos.stream()
                    .anyMatch(c -> c.getCorreo() != null && c.getCorreo().equalsIgnoreCase(cliente.getCorreo().trim()) && !c.getId().equals(cliente.getId()));
            if (existeCorreo) {
                throw new IllegalArgumentException("El correo electrónico '" + cliente.getCorreo() + "' ya le pertenece a otro cliente.");
            }
        }

        // 4. Validar Teléfono único al editar
        if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
            boolean existeTelefono = todos.stream()
                    .anyMatch(c -> c.getTelefono() != null && c.getTelefono().trim().equals(cliente.getTelefono().trim()) && !c.getId().equals(cliente.getId()));
            if (existeTelefono) {
                throw new IllegalArgumentException("El número de teléfono '" + cliente.getTelefono() + "' ya le pertenece a otro cliente.");
            }
        }

        return clienteRepository.update(cliente);
    }

    public void eliminar(Long id, Usuario usuarioActual) {
        if (usuarioActual != null && usuarioActual.getRol() == RolUsuario.CAJERO) {
            throw new IllegalArgumentException("⛔ ALERTA DE SEGURIDAD: Un cajero no tiene permisos para eliminar clientes.");
        }

        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        c.setEstado(EstadoCliente.INACTIVO);
        clienteRepository.update(c);
    }

    public void desactivar(Long id) {
        clienteRepository.findById(id).ifPresent(c -> {
            c.setEstado(EstadoCliente.INACTIVO);
            clienteRepository.update(c);
        });
    }
}