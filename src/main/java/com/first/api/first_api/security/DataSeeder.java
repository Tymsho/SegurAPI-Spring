package com.first.api.first_api.security;

import com.first.api.first_api.models.*;
import com.first.api.first_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@SuppressWarnings("null")
public class DataSeeder implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CompaniaRepository companiaRepository;
    @Autowired private RamoRepository ramoRepository;
    @Autowired private ProvinciaRepository provinciaRepository;
    @Autowired private LocalidadRepository localidadRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        cargarGeografia();
        cargarUsuarios();
        cargarDatosComerciales();
    }

    private void cargarGeografia() {
        if (provinciaRepository.count() == 0) {
            Provincia p1 = new Provincia(); p1.setNombre("Buenos Aires");
            provinciaRepository.save(p1);

            Localidad l1 = new Localidad(); l1.setNombre("Coronel Pringles"); l1.setProvincia(p1);
            Localidad l2 = new Localidad(); l2.setNombre("Bahía Blanca"); l2.setProvincia(p1);
            localidadRepository.saveAll(List.of(l1, l2));
        }
    }

    private void cargarUsuarios() {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setEmail("admin@mail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador Global");
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true); // Forzamos true para poder loguearnos en las pruebas

            Usuario productor = new Usuario();
            productor.setEmail("productor@seguros.com");
            productor.setPassword(passwordEncoder.encode("clave123"));
            productor.setNombre("Productor Asignado");
            productor.setRol(Rol.PRODUCTOR);
            productor.setActivo(true);

            usuarioRepository.saveAll(List.of(admin, productor));
        }
    }

    private void cargarDatosComerciales() {
        if (companiaRepository.count() == 0) {
            Compania c1 = new Compania(); c1.setNombre("Federación Patronal");
            Compania c2 = new Compania(); c2.setNombre("Mapfre");
            companiaRepository.saveAll(List.of(c1, c2));
        }

        if (ramoRepository.count() == 0) {
            Ramo r1 = new Ramo(); r1.setNombre("Automotor");
            Ramo r2 = new Ramo(); r2.setNombre("Combinado Familiar");
            ramoRepository.saveAll(List.of(r1, r2));
        }
    }
}