package com.first.api.first_api.services;

import com.first.api.first_api.models.Cliente;
import com.first.api.first_api.dto.ClienteDTO;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // --- MAPEO MANUAL (Entidad a DTO) ---
    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setDniCuit(cliente.getDniCuit());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        return dto;
    }
    // --- MAPEO INVERSO (DTO a Entidad) ---
    private Cliente convertirAEntidad(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        // No seteamos el ID porque la base de datos lo genera (o lo buscamos si es un PUT)
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setDniCuit(dto.getDniCuit());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        return cliente;
    }

    // --- MÉTODOS DEL SERVICIO (Devuelven DTOs) ---

    // Obtener solo clientes activos
    public List<ClienteDTO> obtenerTodosActivos(String nombre) {
        List<Cliente> clientes;
        if (nombre != null && !nombre.isEmpty()) {
            clientes = clienteRepository.buscarPorNombreYActivo(nombre);
        } else {
            clientes = clienteRepository.findAll().stream()
                    .filter(Cliente::isActivo)
                    .collect(Collectors.toList());
        }
        return clientes.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    // Buscar por ID
    public Optional<ClienteDTO> buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .filter(Cliente::isActivo)
                .map(this::convertirADTO);
    }

    // Guardar (Recibe una Entidad por ahora, pero devuelve el DTO guardado)
    public ClienteDTO guardarCliente(ClienteDTO clienteDTO) {
        Cliente cliente = convertirAEntidad(clienteDTO);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return convertirADTO(clienteGuardado);
    }

    // Baja Lógica (No devuelve nada, pero anula el registro)
    public void bajaLogica(Long id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            cliente.setActivo(false);
            clienteRepository.save(cliente);
        } else {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
    }

    public ClienteDTO actualizarCliente(Long id, ClienteDTO detallesDTO) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        clienteExistente.setNombre(detallesDTO.getNombre());
        clienteExistente.setApellido(detallesDTO.getApellido());
        clienteExistente.setDniCuit(detallesDTO.getDniCuit());
        clienteExistente.setTelefono(detallesDTO.getTelefono());
        clienteExistente.setEmail(detallesDTO.getEmail());

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        return convertirADTO(clienteActualizado);
    }
}