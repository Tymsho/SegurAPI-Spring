package com.first.api.first_api.services;

import com.first.api.first_api.models.Cliente;
import com.first.api.first_api.models.Localidad;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.dto.ClienteDTO;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.repositories.ClienteRepository;
import com.first.api.first_api.repositories.LocalidadRepository;
import com.first.api.first_api.repositories.UsuarioRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    // --- MAPEO MANUAL (Entidad a DTO) ---
    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setDni(cliente.getDni());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        return dto;
    }

    // --- MÉTODOS DEL SERVICIO (Devuelven DTOs) ---

    // Obtener solo clientes activos
    public List<ClienteDTO> obtenerTodosActivos(String nombre) {
        // 1. Obtener el productor logueado
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Cliente> clientes;

        // 2. Filtrar asegurando que solo traiga SU cartera
        if (nombre != null && !nombre.isEmpty()) {
            clientes = clienteRepository.findByNombreContainingIgnoreCaseAndActivoTrueAndProductorEmail(nombre,
                    emailLogueado);
        } else {
            clientes = clienteRepository.findByActivoTrueAndProductorEmail(emailLogueado);
        }

        // 3. Mapear a DTO (esto asume que tenés un método mapearADto en tu servicio)
        return clientes.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Buscar por ID
    public Optional<ClienteDTO> buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .filter(Cliente::isActivo)
                .map(this::convertirADTO);
    }

    // Guardar (Recibe una Entidad por ahora, pero devuelve el DTO guardado)
    public ClienteDTO crearCliente(ClienteDTO dto) {
        // 1. Quién está haciendo la petición?
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario productor = usuarioRepository.findByEmail(emailLogueado)
                .orElseThrow(() -> new RuntimeException("Productor no autorizado"));

        // 2. Validar que no exista el DNI en SU cartera
        if (clienteRepository.existsByDniAndProductorEmail(dto.getDni(), emailLogueado)) {
            throw new RuntimeException("Ya tenés un cliente registrado con este DNI");
        }

        // 3. Buscar las dependencias (Localidad)
        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new RuntimeException("Localidad no encontrada"));

        // 4. Mapear y guardar
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setDni(dto.getDni());
        cliente.setEmail(dto.getEmail());
        cliente.setSexo(dto.getSexo());
        cliente.setTipoIva(dto.getTipoIva());
        cliente.setLocalidad(localidad);
        // ... setear el resto de los campos ...

        // 5. Asignar el dueño (Aislamiento de datos)
        cliente.setProductor(productor);

        Cliente guardado = clienteRepository.save(cliente);

        // Retornar tu DTO mapeado (hacelo según tu lógica actual)
        dto.setId(guardado.getId());
        return dto;
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
        clienteExistente.setDni(detallesDTO.getDni());
        clienteExistente.setTelefono(detallesDTO.getTelefono());
        clienteExistente.setEmail(detallesDTO.getEmail());

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        return convertirADTO(clienteActualizado);
    }
}