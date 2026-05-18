package com.first.api.first_api.repositories;

import com.first.api.first_api.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Necesario para que Spring Security busque al usuario cuando intente loguearse
    Optional<Usuario> findByEmail(String email);
}