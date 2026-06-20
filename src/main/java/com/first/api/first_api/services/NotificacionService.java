package com.first.api.first_api.services;

import com.first.api.first_api.models.Notificacion;
import com.first.api.first_api.models.Usuario;
import com.first.api.first_api.repositories.NotificacionRepository;
import com.first.api.first_api.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void enviarNotificacion(Long usuarioId, String titulo, String mensaje) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);

        Notificacion guardada = notificacionRepository.save(notificacion);

        // Enviar vía STOMP (WebSocket)
        messagingTemplate.convertAndSend("/topic/notificaciones/" + usuarioId, guardada);
    }

    public List<Notificacion> misNotificaciones() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificacionRepository.findByUsuarioEmailOrderByFechaCreacionDesc(email);
    }

    public void marcarComoLeida(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Notificacion notif = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        if (!notif.getUsuario().getEmail().equals(email)) {
            throw new RuntimeException("Acceso denegado a esta notificación");
        }
        
        notif.setLeida(true);
        notificacionRepository.save(notif);
    }
}
