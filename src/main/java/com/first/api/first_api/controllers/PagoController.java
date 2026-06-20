package com.first.api.first_api.controllers;

import com.first.api.first_api.services.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @PostMapping("/premium")
    public ResponseEntity<String> obtenerLinkPagoPremium() {
        String url = pagoService.crearPreferenciaPremium();
        return ResponseEntity.ok(url);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhookMercadoPago(@RequestBody Map<String, Object> payload) {
        // Aquí lo dejaremos genérico como base para la integración
        System.out.println("Webhook recibido: " + payload);
        
        // Simulación temporal para que quede la base funcional de actualizar el plan
        if (payload.containsKey("external_reference")) {
            pagoService.procesarWebhook((String) payload.get("external_reference"));
        }

        return ResponseEntity.ok("OK");
    }
}
