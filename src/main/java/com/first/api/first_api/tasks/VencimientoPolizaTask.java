package com.first.api.first_api.tasks;

import com.first.api.first_api.models.Poliza;
import com.first.api.first_api.repositories.PolizaRepository;
import com.first.api.first_api.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

@Component
public class VencimientoPolizaTask {

    private static final Logger logger = LoggerFactory.getLogger(VencimientoPolizaTask.class);

    @Autowired
    private PolizaRepository polizaRepository;

    @Autowired
    private NotificacionService notificacionService;

    // Se ejecuta todos los días a las 09:00 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void notificarPolizasProximasAVencer() {
        logger.info("Iniciando tarea programada: Verificación de pólizas próximas a vencer.");

        // Notificar a los 30, 15, y 7 días antes de vencer
        int[] diasAviso = {30, 15, 7, 1};

        for (int dias : diasAviso) {
            LocalDate fechaObjetivo = LocalDate.now().plusDays(dias);
            List<Poliza> polizasVenciendo = polizaRepository.findPolizasVenciendoEn(fechaObjetivo);

            for (Poliza poliza : polizasVenciendo) {
                String mensaje = String.format("La póliza %s del cliente %s (%s) vence en %d días.",
                        poliza.getNroPza(),
                        poliza.getTomador().getNombre(),
                        poliza.getCompania().getNombre(),
                        dias);
                
                logger.info("Enviando notificación al productor ID {}: {}", poliza.getProductor().getId(), mensaje);

                notificacionService.enviarNotificacion(
                        poliza.getProductor().getId(),
                        "Póliza próxima a vencer",
                        mensaje
                );
            }
        }
        logger.info("Finalizada la tarea programada de verificación de vencimientos.");
    }
}
