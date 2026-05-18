package com.first.api.first_api.controllers;

import com.first.api.first_api.dto.ClienteDTO;
import com.first.api.first_api.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteDTO> crearCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO nuevoCliente = clienteService.crearCliente(clienteDTO);
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> obtenerClientes(
            @RequestParam(value = "nombre", required = false) String nombre) {
        // El servicio ya sabe que tiene que traer solo los de este productor
        List<ClienteDTO> clientes = clienteService.obtenerTodosActivos(nombre);
        return ResponseEntity.ok(clientes);
    }

    // Opcional: Agregar endpoints de PUT (actualizar) y DELETE (anular) 
    // siguiendo la misma estructura que hicimos en Pólizas.
}