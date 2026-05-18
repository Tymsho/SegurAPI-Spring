package com.first.api.first_api.services;

import com.first.api.first_api.models.*;
import com.first.api.first_api.dto.PolizaDTO;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PolizaService {

    @Autowired private PolizaRepository polizaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RamoRepository ramoRepository;
    @Autowired private CompaniaRepository companiaRepository;

    // --- UTILIDAD: Obtener el Productor Logueado ---
    private String getEmailLogueado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Usuario getProductorLogueado() {
        String email = getEmailLogueado();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Productor no autorizado o no encontrado"));
    }

    // --- MAPEO MANUAL (Entidad a DTO) ---
    private PolizaDTO convertirADTO(Poliza poliza) {
        PolizaDTO dto = new PolizaDTO();
        dto.setId(poliza.getId());
        dto.setNroPza(poliza.getNroPza());
        dto.setInicioVigencia(poliza.getInicioVigencia());
        dto.setFinVigencia(poliza.getFinVigencia());
        dto.setTipoPago(poliza.getTipoPago());
        dto.setTipoFacturacion(poliza.getTipoFacturacion());
        dto.setPrima(poliza.getPrima());
        dto.setPremio(poliza.getPremio());
        
        if (poliza.getTomador() != null) {
            dto.setClienteId(poliza.getTomador().getId());
        }
        if (poliza.getCompania() != null) {
            dto.setCompaniaId(poliza.getCompania().getId());
        }
        if (poliza.getRamo() != null) {
            dto.setRamoId(poliza.getRamo().getId());
        }
        
        return dto;
    }

    // --- MAPEO INVERSO (DTO a Entidad) ---
    private Poliza convertirAEntidad(PolizaDTO dto, String emailLogueado) {
        Poliza poliza = new Poliza();
        poliza.setNroPza(dto.getNroPza());
        poliza.setInicioVigencia(dto.getInicioVigencia());
        poliza.setFinVigencia(dto.getFinVigencia());
        poliza.setTipoPago(dto.getTipoPago());
        poliza.setTipoFacturacion(dto.getTipoFacturacion());
        poliza.setPrima(dto.getPrima());
        poliza.setPremio(dto.getPremio());

        // 1. Validar y asignar el Cliente (Garantizando que sea de SU cartera)
        Cliente tomador = clienteRepository.findByIdAndProductorEmail(dto.getClienteId(), emailLogueado)
                .orElseThrow(() -> new ResourceNotFoundException("El cliente no existe o no pertenece a tu cartera"));
        poliza.setTomador(tomador);

        // 2. Validar y asignar Ramo y Compañía
        Compania compania = companiaRepository.findById(dto.getCompaniaId())
                .orElseThrow(() -> new ResourceNotFoundException("Compañía no encontrada"));
        poliza.setCompania(compania);

        Ramo ramo = ramoRepository.findById(dto.getRamoId())
                .orElseThrow(() -> new ResourceNotFoundException("Ramo no encontrado"));
        poliza.setRamo(ramo);
        
        return poliza;
    }

    // --- MÉTODOS DEL SERVICIO ---

    public PolizaDTO guardarPoliza(PolizaDTO polizaDTO) {
        String email = getEmailLogueado();
        Usuario productor = getProductorLogueado();

        Poliza poliza = convertirAEntidad(polizaDTO, email);
        
        // Sellamos la póliza con la firma del productor logueado
        poliza.setProductor(productor);
        
        Poliza guardada = polizaRepository.save(poliza);
        return convertirADTO(guardada);
    }

    public Optional<PolizaDTO> buscarPorId(Long id) {
        // Solo puede buscarla si le pertenece a él
        return polizaRepository.findByIdAndProductorEmail(id, getEmailLogueado())
                .map(this::convertirADTO);
    }

    public List<PolizaDTO> obtenerMisPolizasActivas() {
        // Eliminamos el parámetro usuarioId. El sistema sabe quién es por el Token.
        return polizaRepository.findByProductorEmail(getEmailLogueado()).stream()
                .filter(Poliza::isActivo)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public void anularPoliza(Long id) {
        // Valida propiedad antes de anular
        Poliza poliza = polizaRepository.findByIdAndProductorEmail(id, getEmailLogueado())
                .orElseThrow(() -> new ResourceNotFoundException("Póliza no encontrada o acceso denegado"));
        
        poliza.setActivo(false);
        polizaRepository.save(poliza);
    }

    public PolizaDTO actualizarPoliza(Long id, PolizaDTO detallesDTO) {
        String email = getEmailLogueado();

        // 1. Asegurar que la póliza a editar es suya
        Poliza polizaExistente = polizaRepository.findByIdAndProductorEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza no encontrada o acceso denegado"));

        // 2. Mapear los datos nuevos validando las FK
        Poliza actualizacion = convertirAEntidad(detallesDTO, email);
        
        polizaExistente.setNroPza(actualizacion.getNroPza());
        polizaExistente.setInicioVigencia(actualizacion.getInicioVigencia());
        polizaExistente.setFinVigencia(actualizacion.getFinVigencia());
        polizaExistente.setTipoPago(actualizacion.getTipoPago());
        polizaExistente.setTipoFacturacion(actualizacion.getTipoFacturacion());
        polizaExistente.setPrima(actualizacion.getPrima());
        polizaExistente.setPremio(actualizacion.getPremio());
        
        polizaExistente.setTomador(actualizacion.getTomador());
        polizaExistente.setCompania(actualizacion.getCompania());
        polizaExistente.setRamo(actualizacion.getRamo());

        Poliza polizaActualizada = polizaRepository.save(polizaExistente);
        return convertirADTO(polizaActualizada);
    }
}