package com.first.api.first_api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@SuppressWarnings("null")
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Async

    public void enviarCorreoVerificacion(String destinatario, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Verificación de cuenta - SegurAPI");
        mensaje.setText("Hola,\n\nGracias por registrarte. Tu código de verificación es: " + codigo 
            + "\n\nEste código vencerá en 15 minutos.\n\nSaludos,\nEl equipo de SegurAPI");
        
        mailSender.send(mensaje);
    }

    @Async
    public void enviarCorreoHtml(String destinatario, String asunto, String plantilla, Context contexto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);

            String htmlContent = templateEngine.process(plantilla, contexto);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error al enviar el correo HTML: " + e.getMessage());
        }
    }
}
