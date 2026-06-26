package com.first.api.first_api.services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.first.api.first_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Collections;

@Service
public class PagoService {

    @Value("${mp.access.token}")
    private String mpAccessToken;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(mpAccessToken);
    }

    public String crearPreferenciaPremium() {
        if ("TEST-TU-TOKEN-AQUI".equals(mpAccessToken) || mpAccessToken == null) {
            // Si el usuario aún no configuró su token real, devolvemos un link simulado
            // para que el frontend no se rompa y el flujo siga funcionando visualmente.
            return "https://www.mercadopago.com.ar/developers/es/docs";
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        try {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id("plan-premium")
                    .title("Suscripción Plan Premium")
                    .description("Acceso a funcionalidades premium de SegurAPI")
                    .categoryId("services")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(new BigDecimal("5000"))
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(Collections.singletonList(itemRequest))
                    .externalReference(email) // Guardamos el email del usuario como ref externa
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getInitPoint(); // URL para redirigir al usuario
        } catch (MPApiException apiException) {
            System.err.println("MERCADOPAGO API ERROR DETAIL: " + apiException.getApiResponse().getContent());
            throw new RuntimeException("Error al crear la preferencia de pago en MercadoPago", apiException);
        } catch (MPException e) {
            System.err.println("MERCADOPAGO SDK ERROR: " + e.getMessage());
            throw new RuntimeException("Error interno de MercadoPago", e);
        }
    }

    @Transactional
    public void procesarWebhook(String externalReference) {
        // En una implementación real, se verifica el estado del pago consultando la API de MP usando el payment_id.
        // Aquí asumimos que el webhook solo se llama (o lo filtramos) cuando el pago es 'approved'.
        // externalReference contiene el email del usuario.
        if (externalReference != null) {
            usuarioRepository.findByEmail(externalReference).ifPresent(usuario -> {
                usuario.setPlan("PREMIUM");
                usuarioRepository.save(usuario);
            });
        }
    }
}
