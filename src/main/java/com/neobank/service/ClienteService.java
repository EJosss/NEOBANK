package com.neobank.service;

import com.neobank.model.Cliente;
import com.neobank.model.enums.EstadoCliente;
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
        if (clienteRepository.existsByDni(cliente.getDni())) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con DNI: " + cliente.getDni()
            );
        }
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return (List<Cliente>) clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarPorDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombreContainsIgnoreCase(nombre);
    }

    public Cliente actualizar(Cliente cliente) {
        return clienteRepository.update(cliente);
    }

    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }

    public void desactivar(Long id) {
        clienteRepository.findById(id).ifPresent(c -> {
            c.setEstado(EstadoCliente.INACTIVO);
            clienteRepository.update(c);
        });
    }
}