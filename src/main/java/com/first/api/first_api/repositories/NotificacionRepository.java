package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioEmailOrderByFechaCreacionDesc(String email);
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}
