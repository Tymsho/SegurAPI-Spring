package com.first.api.first_api.services;

import com.first.api.first_api.models.Cliente;
import com.first.api.first_api.models.Localidad;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.dtorequest.ClienteRequest;
import com.first.api.first_api.dtoresponse.ClienteResponse;
import com.first.api.first_api.exceptions.DuplicateResourceException;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.repositories.ClienteRepository;
import com.first.api.first_api.repositories.LocalidadRepository;
import com.first.api.first_api.repositories.UsuarioRepository;

import org.springframework.security.core.Authentication;
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

    // --- UTILIDAD: Obtener el Productor Logueado ---
    private String getEmailLogueado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    // --- MÉTODOS DEL SERVICIO (Devuelven DTOs) ---

    // Obtener solo clientes activos
    public Page<ClienteResponse> obtenerTodosActivos(String nombre, Pageable pageable) {
        String emailLogueado = getEmailLogueado();

        Page<Cliente> clientes;

        // Filtrar asegurando que solo traiga SU cartera si hay email
        if (nombre != null && !nombre.isEmpty()) {
            clientes = clienteRepository.findByNombreContainingIgnoreCaseAndActivoTrueAndProductorEmail(nombre,
                    emailLogueado, pageable);
        } else {
            clientes = clienteRepository.findByActivoTrueAndProductorEmail(emailLogueado, pageable);
        }

        return clientes.map(clienteMapper::toResponse);
    }

    public java.util.List<com.first.api.first_api.dtoresponse.ClienteLightResponse> buscarClientesLight(String query) {
        String emailLogueado = getEmailLogueado();
        return clienteRepository.searchClientesLight(emailLogueado, query, org.springframework.data.domain.PageRequest.of(0, 10));
    }

    // Buscar por ID
    public Optional<ClienteResponse> buscarPorId(Long id) {
        String email = getEmailLogueado();
        Optional<Cliente> cliente = (email != null)
                ? clienteRepository.findByIdAndProductorEmail(id, email)
                : clienteRepository.findById(id);
        return cliente.filter(Cliente::isActivo).map(clienteMapper::toResponse);
    }

    // Guardar (Recibe una Entidad por ahora, pero devuelve el DTO guardado)
    @Transactional
    public ClienteResponse crearCliente(ClienteRequest dto) {
        String emailLogueado = getEmailLogueado();

        Usuario productor = usuarioRepository.findByEmail(emailLogueado)
                .orElseThrow(() -> new ResourceNotFoundException("Productor no autorizado"));

        // Validar que no exista el DNI en SU cartera
        if (clienteRepository.existsByDniAndProductorEmail(dto.getDni(), emailLogueado)) {
            throw new DuplicateResourceException("Ya tenés un cliente registrado con este DNI");
        }

        // Buscar las dependencias (Localidad)
        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new ResourceNotFoundException("Localidad no encontrada"));

        // Mapear y guardar
        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setLocalidad(localidad);

        // Asignar el dueño (Aislamiento de datos)
        cliente.setProductor(productor);

        Cliente guardado = clienteRepository.save(cliente);

        return clienteMapper.toResponse(guardado);
    }

    // Baja Lógica (No devuelve nada, pero anula el registro)
    @Transactional
    public void bajaLogica(Long id) {
        String email = getEmailLogueado();
        Optional<Cliente> clienteOpt = (email != null)
                ? clienteRepository.findByIdAndProductorEmail(id, email)
                : clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            cliente.setActivo(false);
            clienteRepository.save(cliente);
        } else {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
    }

    @Transactional
    public ClienteResponse actualizarCliente(Long id, ClienteRequest detallesDTO) {
        String email = getEmailLogueado();
        Cliente clienteExistente = (email != null ? clienteRepository.findByIdAndProductorEmail(id, email) : clienteRepository.findById(id))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        // Si cambió el DNI, verificar que no esté duplicado en su cartera
        if (detallesDTO.getDni() != null && !detallesDTO.getDni().equals(clienteExistente.getDni())) {
            if (email != null && clienteRepository.existsByDniAndProductorEmail(detallesDTO.getDni(), email)) {
                throw new DuplicateResourceException("Ya tenés un cliente registrado con este DNI");
            }
        }

        clienteExistente.setNombre(detallesDTO.getNombre());
        clienteExistente.setApellido(detallesDTO.getApellido());
        clienteExistente.setDni(detallesDTO.getDni());
        clienteExistente.setTelefono(detallesDTO.getTelefono());
        clienteExistente.setEmail(detallesDTO.getEmail());
        
        // Agregar campos faltantes
        clienteExistente.setFechaNacimiento(detallesDTO.getFechaNacimiento());
        clienteExistente.setDireccion(detallesDTO.getDireccion());
        clienteExistente.setSexo(detallesDTO.getSexo());
        clienteExistente.setTipoIva(detallesDTO.getTipoIva());

        // Si cambia localidad
        if (detallesDTO.getLocalidadId() != null) {
            Localidad localidad = localidadRepository.findById(detallesDTO.getLocalidadId())
                .orElseThrow(() -> new ResourceNotFoundException("Localidad no encontrada"));
            clienteExistente.setLocalidad(localidad);
        }

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        
        return clienteMapper.toResponse(clienteActualizado);
    }
}