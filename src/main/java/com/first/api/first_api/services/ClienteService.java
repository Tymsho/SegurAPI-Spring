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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import com.first.api.first_api.mappers.ClienteMapper;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private ClienteMapper clienteMapper;

    // --- MÉTODOS DEL SERVICIO (Devuelven DTOs) ---

    // Obtener solo clientes activos
    public Page<ClienteDTO> obtenerTodosActivos(String nombre, Pageable pageable) {
        // 1. Obtener el productor logueado
        String emailLogueado = SecurityContextHolder.getContext().getAuthentication().getName();

        Page<Cliente> clientes;

        // 2. Filtrar asegurando que solo traiga SU cartera
        if (nombre != null && !nombre.isEmpty()) {
            clientes = clienteRepository.findByNombreContainingIgnoreCaseAndActivoTrueAndProductorEmail(nombre,
                    emailLogueado, pageable);
        } else {
            clientes = clienteRepository.findByActivoTrueAndProductorEmail(emailLogueado, pageable);
        }

        // 3. Mapear a DTO
        return clientes.map(clienteMapper::toDTO);
    }

    // Buscar por ID
    public Optional<ClienteDTO> buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .filter(Cliente::isActivo)
                .map(clienteMapper::toDTO);
    }

    // Guardar (Recibe una Entidad por ahora, pero devuelve el DTO guardado)
    @Transactional
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
        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setLocalidad(localidad);

        // 5. Asignar el dueño (Aislamiento de datos)
        cliente.setProductor(productor);

        Cliente guardado = clienteRepository.save(cliente);

        // Retornar tu DTO mapeado
        return clienteMapper.toDTO(guardado);
    }

    // Baja Lógica (No devuelve nada, pero anula el registro)
    @Transactional
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

    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO detallesDTO) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        clienteExistente.setNombre(detallesDTO.getNombre());
        clienteExistente.setApellido(detallesDTO.getApellido());
        clienteExistente.setDni(detallesDTO.getDni());
        clienteExistente.setTelefono(detallesDTO.getTelefono());
        clienteExistente.setEmail(detallesDTO.getEmail());

        // Si cambia localidad
        if (detallesDTO.getLocalidadId() != null) {
            Localidad localidad = localidadRepository.findById(detallesDTO.getLocalidadId())
                .orElseThrow(() -> new RuntimeException("Localidad no encontrada"));
            clienteExistente.setLocalidad(localidad);
        }

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        return clienteMapper.toDTO(clienteActualizado);
    }
}