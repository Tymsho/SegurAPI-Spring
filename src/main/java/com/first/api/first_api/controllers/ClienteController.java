package com.first.api.first_api.controllers;

import com.first.api.first_api.dtorequest.ClienteRequest;
import com.first.api.first_api.dtoresponse.ClienteResponse;
import com.first.api.first_api.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@Valid @RequestBody ClienteRequest clienteDTO) {
        ClienteResponse nuevoCliente = clienteService.crearCliente(clienteDTO);
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> obtenerClientes(
            @RequestParam(value = "nombre", required = false) String nombre,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ClienteResponse> clientes = clienteService.obtenerTodosActivos(nombre, pageable);
        return ResponseEntity.ok(clientes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteRequest clienteDTO) {
        ClienteResponse actualizado = clienteService.actualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anularCliente(@PathVariable Long id) {
        clienteService.bajaLogica(id);
        return ResponseEntity.noContent().build();
    }
}