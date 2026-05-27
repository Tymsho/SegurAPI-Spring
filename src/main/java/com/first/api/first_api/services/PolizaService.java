package com.first.api.first_api.services;

import com.first.api.first_api.models.*;
import com.first.api.first_api.dtorequest.PolizaRequest;
import com.first.api.first_api.dtoresponse.PolizaResponse;
import com.first.api.first_api.exceptions.ResourceNotFoundException;
import com.first.api.first_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import com.first.api.first_api.mappers.PolizaMapper;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class PolizaService {

    @Autowired private PolizaRepository polizaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RamoRepository ramoRepository;
    @Autowired private CompaniaRepository companiaRepository;
    @Autowired private PolizaMapper polizaMapper;

    // --- UTILIDAD: Obtener el Productor Logueado ---
    private String getEmailLogueado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Usuario getProductorLogueado() {
        String email = getEmailLogueado();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Productor no autorizado o no encontrado"));
    }

    // --- MAPEO INVERSO (DTO a Entidad) ---
    private Poliza convertirAEntidad(PolizaRequest dto, String emailLogueado) {
        Poliza poliza = polizaMapper.toEntity(dto);

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

    @Transactional
    public PolizaResponse guardarPoliza(PolizaRequest polizaDTO) {
        String email = getEmailLogueado();
        Usuario productor = getProductorLogueado();

        Poliza poliza = convertirAEntidad(polizaDTO, email);
        
        // Sellamos la póliza con la firma del productor logueado
        poliza.setProductor(productor);
        
        Poliza guardada = polizaRepository.save(poliza);
        return polizaMapper.toResponse(guardada);
    }

    public Optional<PolizaResponse> buscarPorId(Long id) {
        // Solo puede buscarla si le pertenece a él
        return polizaRepository.findByIdAndProductorEmail(id, getEmailLogueado())
                .map(polizaMapper::toResponse);
    }

    public Page<PolizaResponse> obtenerMisPolizasActivas(Long clienteId, Long companiaId, Long ramoId, Pageable pageable) {
        return polizaRepository.findMisPolizasFiltradas(getEmailLogueado(), clienteId, companiaId, ramoId, pageable)
                .map(polizaMapper::toResponse);
    }

    @Transactional
    public void anularPoliza(Long id) {
        // Valida propiedad antes de anular
        Poliza poliza = polizaRepository.findByIdAndProductorEmail(id, getEmailLogueado())
                .orElseThrow(() -> new ResourceNotFoundException("Póliza no encontrada o acceso denegado"));
        
        poliza.setActivo(false);
        polizaRepository.save(poliza);
    }

    @Transactional
    public PolizaResponse actualizarPoliza(Long id, PolizaRequest detallesDTO) {
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
        return polizaMapper.toResponse(polizaActualizada);
    }
}