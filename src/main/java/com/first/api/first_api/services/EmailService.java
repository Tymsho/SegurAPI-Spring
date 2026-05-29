package com.first.api.first_api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoVerificacion(String destinatario, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Verificación de cuenta - SegurAPI");
        mensaje.setText("Hola,\n\nGracias por registrarte. Tu código de verificación es: " + codigo 
            + "\n\nEste código vencerá en 15 minutos.\n\nSaludos,\nEl equipo de SegurAPI");
        
        mailSender.send(mensaje);
    }
}
