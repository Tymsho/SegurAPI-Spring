package com.first.api.first_api.services;

import com.first.api.first_api.models.Cliente;
import com.first.api.first_api.models.Compania;
import com.first.api.first_api.models.Poliza;
import com.first.api.first_api.models.Ramo;
import com.first.api.first_api.dto.PolizaDTO;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.repositories.PolizaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PolizaService {

    @Autowired
    private PolizaRepository polizaRepository;

    // --- MAPEO MANUAL (Entidad a DTO) ---
    private PolizaDTO convertirADTO(Poliza poliza) {
        PolizaDTO dto = new PolizaDTO();
        dto.setId(poliza.getId());
        dto.setNumeroPoliza(poliza.getNumeroPoliza());
        dto.setFechaInicio(poliza.getFechaInicio());
        dto.setFechaFin(poliza.getFechaFin());
        
        // Extraemos solo los nombres (¡Acá está la magia del DTO!)
        if (poliza.getCliente() != null) {
            dto.setNombreCliente(poliza.getCliente().getNombre() + " " + poliza.getCliente().getApellido());
            dto.setClienteId(poliza.getCliente().getId()); // Nuevo
        }
        if (poliza.getCompania() != null) {
            dto.setNombreCompania(poliza.getCompania().getNombre());
            dto.setCompaniaId(poliza.getCompania().getId()); // Nuevo
        }
        if (poliza.getRamo() != null) {
            dto.setNombreRamo(poliza.getRamo().getNombre());
            dto.setRamoId(poliza.getRamo().getId()); // Nuevo
        }
        
        return dto;
    }

    // --- MAPEO INVERSO (DTO a Entidad) ---
    private Poliza convertirAEntidad(PolizaDTO dto) {
        Poliza poliza = new Poliza();
        poliza.setNumeroPoliza(dto.getNumeroPoliza());
        poliza.setFechaInicio(dto.getFechaInicio());
        poliza.setFechaFin(dto.getFechaFin());

        if (dto.getClienteId() != null) {
            Cliente cliente = new Cliente();
            cliente.setId(dto.getClienteId());
            poliza.setCliente(cliente);
        }
        if (dto.getCompaniaId() != null) {
            Compania compania = new Compania();
            compania.setId(dto.getCompaniaId());
            poliza.setCompania(compania);
        }
        if (dto.getRamoId() != null) {
            Ramo ramo = new Ramo();
            ramo.setId(dto.getRamoId());
            poliza.setRamo(ramo);
        }
        
        return poliza;
    }

    // --- MÉTODOS DEL SERVICIO ---

    public PolizaDTO guardarPoliza(PolizaDTO polizaDTO) {
        Poliza poliza = convertirAEntidad(polizaDTO);
        Poliza guardada = polizaRepository.save(poliza);
        return convertirADTO(guardada);
    }

    public Optional<PolizaDTO> buscarPorId(Long id) {
        return polizaRepository.findById(id).map(this::convertirADTO);
    }

    public List<PolizaDTO> obtenerPolizasActivas() {
        return polizaRepository.findByActivaTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<PolizaDTO> obtenerPolizasPorUsuario(Long usuarioId) {
        return polizaRepository.findByUsuarioId(usuarioId).stream()
                .filter(Poliza::isActiva) // Solo las activas
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public void anularPoliza(Long id) {
        Optional<Poliza> polizaOpt = polizaRepository.findById(id);
        if (polizaOpt.isPresent()) {
            Poliza poliza = polizaOpt.get();
            poliza.setActiva(false);
            polizaRepository.save(poliza);
        } else {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
    }

    public PolizaDTO actualizarPoliza(Long id, PolizaDTO detallesDTO) {
        Poliza polizaExistente = polizaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza no encontrada con id: " + id));

        Poliza actualizacion = convertirAEntidad(detallesDTO);
        
        polizaExistente.setNumeroPoliza(actualizacion.getNumeroPoliza());
        polizaExistente.setFechaInicio(actualizacion.getFechaInicio());
        polizaExistente.setFechaFin(actualizacion.getFechaFin());
        polizaExistente.setCliente(actualizacion.getCliente());
        polizaExistente.setCompania(actualizacion.getCompania());
        polizaExistente.setRamo(actualizacion.getRamo());

        Poliza polizaActualizada = polizaRepository.save(polizaExistente);
        return convertirADTO(polizaActualizada);
    }
}