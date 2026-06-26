package com.first.api.first_api.security;

import com.first.api.first_api.models.*;
import com.first.api.first_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("null")
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CompaniaRepository companiaRepository;
    @Autowired
    private RamoRepository ramoRepository;
    @Autowired
    private ProvinciaRepository provinciaRepository;
    @Autowired
    private LocalidadRepository localidadRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PolizaRepository polizaRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        cargarGeografia();
        cargarUsuarios();
        cargarDatosComerciales();
        cargarClientesYPolizas();
    }

    private void cargarGeografia() {
        if (provinciaRepository.count() == 0) {
            Provincia p1 = new Provincia();
            p1.setNombre("Buenos Aires");
            provinciaRepository.save(p1);

            Localidad l1 = new Localidad();
            l1.setNombre("Coronel Pringles");
            l1.setProvincia(p1);
            Localidad l2 = new Localidad();
            l2.setNombre("Bahía Blanca");
            l2.setProvincia(p1);
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
            admin.setActivo(true);

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
        List<String> nombresCompanias = Arrays.asList(
            "Federación Patronal Seguros", "Sancor Seguros", "La Caja de Ahorro y Seguro",
            "Seguros Rivadavia", "San Cristóbal Seguros", "Mercantil Andina", "La Segunda",
            "Zurich Argentina", "Allianz Argentina", "Mapfre Argentina", "La Holando Sudamericana",
            "Provincia Seguros", "Nación Seguros", "Libra Seguros", "El Norte Seguros",
            "La Perseverancia Seguros", "Cooperación Seguros", "MetLife Seguros", "SMG LIFE",
            "Galeno Life", "Prudential Seguros", "Zurich International Life", "La Estrella",
            "Orígenes Seguros", "Binaria Seguros de Vida", "Prevención ART", "Asociart ART",
            "Provincia ART", "Galeno ART", "Experta ART", "Swiss Medical ART", "La Segunda ART",
            "Aseguradores de Cauciones", "Afianzadora Latinoamericana", "Crédito y Caución", "Insur"
        );

        List<String> existentesCompanias = companiaRepository.findAll().stream().map(Compania::getNombre).collect(Collectors.toList());
        for (String nombre : nombresCompanias) {
            if (!existentesCompanias.contains(nombre) && !existentesCompanias.contains(nombre.replace(" Seguros", ""))) {
                Compania c = new Compania();
                c.setNombre(nombre);
                companiaRepository.save(c);
            }
        }

        List<String> nombresRamos = Arrays.asList(
            "Automotores", "Motovehículos", "Combinado Familiar", "Integral de Comercio / Consorcio",
            "Incendio", "Transporte (Cascos y Carga)", "Caución", "Responsabilidad Civil",
            "Riesgos Agrícolas / Granizo", "Técnico", "Crédito", "Vida Individual",
            "Vida Colectivo", "Vida Obligatorio", "Accidentes Personales (AP)", "Retiro",
            "Sepelio", "Riesgos del Trabajo (ART)"
        );

        List<String> existentesRamos = ramoRepository.findAll().stream().map(Ramo::getNombre).collect(Collectors.toList());
        for (String nombre : nombresRamos) {
            if (!existentesRamos.contains(nombre) && !existentesRamos.contains(nombre.replace("Automotores", "Automotor"))) {
                Ramo r = new Ramo();
                r.setNombre(nombre);
                ramoRepository.save(r);
            }
        }
    }

    private void cargarClientesYPolizas() {
        Usuario productor = usuarioRepository.findByEmail("productor@seguros.com").orElse(null);
        if (productor == null) return;

        Localidad localidad = localidadRepository.findAll().stream().findFirst().orElse(null);

        String[] nombres = {"Juan", "Maria", "Carlos", "Lucia", "Jorge", "Ana", "Pedro", "Marta", "Diego", "Sofia"};
        String[] apellidos = {"Perez", "Gomez", "Rodriguez", "Fernandez", "Lopez", "Martinez", "Gonzalez", "Romero", "Sosa", "Diaz"};
        
        List<Cliente> clientesActivos = new ArrayList<>();
        
        // Ensure at least 10 clients exist
        for (int i = 0; i < 10; i++) {
            final int index = i;
            String dni = "1000000" + index;
            Cliente c = clienteRepository.findAll().stream()
                    .filter(cl -> dni.equals(cl.getDni()))
                    .findFirst()
                    .orElseGet(() -> {
                        Cliente nuevo = new Cliente();
                        nuevo.setNombre(nombres[index]);
                        nuevo.setApellido(apellidos[index]);
                        nuevo.setFechaNacimiento(LocalDate.of(1970 + index * 2, (index % 12) + 1, (index % 28) + 1));
                        nuevo.setDireccion("Calle Falsa " + (123 + index * 10));
                        nuevo.setLocalidad(localidad);
                        nuevo.setTelefono("291555" + index + index + index + index);
                        nuevo.setEmail(nombres[index].toLowerCase() + "." + apellidos[index].toLowerCase() + "@example.com");
                        nuevo.setDni(dni);
                        nuevo.setSexo(index % 2 == 0 ? Sexo.MASCULINO : Sexo.FEMENINO);
                        nuevo.setTipoIva(TipoIva.CONSUMIDOR_FINAL);
                        nuevo.setProductor(productor);
                        nuevo.setActivo(true);
                        return clienteRepository.save(nuevo);
                    });
            clientesActivos.add(c);
        }

        // Generate a realistic portfolio if policies count is low (less than 10)
        if (polizaRepository.count() < 10) {
            List<Compania> companias = companiaRepository.findAll();
            List<Ramo> ramos = ramoRepository.findAll();
            Random rand = new Random();

            for (int i = 0; i < 35; i++) {
                Cliente cliente = clientesActivos.get(rand.nextInt(clientesActivos.size()));
                Compania compania = companias.get(rand.nextInt(companias.size()));
                Ramo ramo = ramos.get(rand.nextInt(ramos.size()));
                
                // Random issue date within the last 12 months
                int daysAgo = rand.nextInt(365);
                LocalDate inicio = LocalDate.now().minusDays(daysAgo);
                
                // 1 year term usually
                LocalDate fin = inicio.plusYears(1);
                
                // Random prime between 5000 and 150000
                double prima = 5000 + (rand.nextDouble() * 145000);
                double premio = prima * 1.21; // add VAT roughly
                
                TipoPago[] pagos = TipoPago.values();
                TipoPago pago = pagos[rand.nextInt(pagos.length)];
                
                Poliza p = new Poliza();
                p.setNroPza("POL-" + ramo.getNombre().substring(0, 3).toUpperCase() + "-" + (1000 + i));
                p.setTomador(cliente);
                p.setTipoPago(pago);
                p.setProductor(productor);
                p.setInicioVigencia(inicio);
                p.setFinVigencia(fin);
                p.setRamo(ramo);
                p.setCompania(compania);
                p.setTipoFacturacion(rand.nextBoolean() ? "Mensual" : "Anual");
                p.setPrima(Math.round(prima * 100.0) / 100.0);
                p.setPremio(Math.round(premio * 100.0) / 100.0);
                p.setActivo(true);
                
                polizaRepository.save(p);
            }
        }
    }
}