package com.first.api.first_api.security;

import com.first.api.first_api.models.*;
import com.first.api.first_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private CompaniaRepository companiaRepository;
    @Autowired
    private RamoRepository ramoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        cargarRolesYUsuarios();
        cargarDatosComerciales();
    }

    private void cargarRolesYUsuarios() {
        if (rolRepository.count() == 0) {
            Rol adminRol = new Rol(); adminRol.setNombre("ROLE_ADMIN");
            Rol userRol = new Rol(); userRol.setNombre("ROLE_USER");
            rolRepository.saveAll(Set.of(adminRol, userRol));

            Usuario admin = new Usuario();
            admin.setEmail("admin@mail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador Global");
            admin.setRoles(Set.of(adminRol));
            admin.setActivo(true);

            Usuario productor = new Usuario();
            productor.setEmail("productor@seguros.com");
            productor.setPassword(passwordEncoder.encode("clave123"));
            productor.setNombre("Productor Asignado");
            productor.setRoles(Set.of(userRol));
            productor.setActivo(true);

            usuarioRepository.saveAll(Set.of(admin, productor));
            System.out.println("Usuarios y Roles inicializados.");
        }
    }

    private void cargarDatosComerciales() {
        if (companiaRepository.count() == 0) {
            Compania c1 = new Compania(); c1.setNombre("Federación Patronal"); c1.setActivo(true);
            Compania c2 = new Compania(); c2.setNombre("Mapfre"); c2.setActivo(true);
            Compania c3 = new Compania(); c3.setNombre("Sancor Seguros"); c3.setActivo(true);
            companiaRepository.saveAll(Set.of(c1, c2, c3));
        }

        if (ramoRepository.count() == 0) {
            Ramo r1 = new Ramo(); r1.setNombre("Automotor"); r1.setActivo(true);
            Ramo r2 = new Ramo(); r2.setNombre("Combinado Familiar"); r2.setActivo(true);
            Ramo r3 = new Ramo(); r3.setNombre("Vida Individual"); r3.setActivo(true);
            ramoRepository.saveAll(Set.of(r1, r2, r3));
            System.out.println("Compañías y Ramos inicializados.");
        }
    }
}