package com.first.api.first_api.controllers;

import com.first.api.first_api.dto.ClienteDTO;
import com.first.api.first_api.services.ClienteService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // GET: Listar todos los clientes activos
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> obtenerClientes(@RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(clienteService.obtenerTodosActivos(nombre));
    }

    // GET: Buscar un cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarClientePorId(@PathVariable Long id) {
        Optional<ClienteDTO> clienteDTO = clienteService.buscarPorId(id);
        return clienteDTO.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST: Crear un nuevo cliente (recibe un JSON de Cliente, devuelve un ClienteDTO)
    @PostMapping
    public ResponseEntity<ClienteDTO> crearCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO nuevoCliente = clienteService.guardarCliente(clienteDTO);
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    // DELETE: Baja lógica de un cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBajaCliente(@PathVariable Long id) {
        clienteService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO actualizado = clienteService.actualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(actualizado);
    }
}