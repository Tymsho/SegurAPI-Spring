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
            Compania c1 = new Compania();
            c1.setNombre("Federación Patronal");
            Compania c2 = new Compania();
            c2.setNombre("Mapfre");
            companiaRepository.saveAll(List.of(c1, c2));
        }

        if (ramoRepository.count() == 0) {
            Ramo r1 = new Ramo();
            r1.setNombre("Automotor");
            Ramo r2 = new Ramo();
            r2.setNombre("Combinado Familiar");
            ramoRepository.saveAll(List.of(r1, r2));
        }
    }

    private void cargarClientesYPolizas() {
        Usuario productor = usuarioRepository.findByEmail("productor@seguros.com").orElse(null);
        if (productor != null) {
            Localidad localidad = localidadRepository.findAll().stream().findFirst().orElse(null);

            // Buscamos o creamos los clientes de prueba según su DNI para no duplicarlos
            Cliente c1 = clienteRepository.findAll().stream()
                    .filter(c -> "12345678".equals(c.getDni()))
                    .findFirst()
                    .orElseGet(() -> {
                        Cliente c = new Cliente();
                        c.setNombre("Juan");
                        c.setApellido("Perez");
                        c.setFechaNacimiento(java.time.LocalDate.of(1985, 5, 15));
                        c.setDireccion("Avenida Alem 123");
                        c.setLocalidad(localidad);
                        c.setTelefono("2914567890");
                        c.setEmail("juan.perez@example.com");
                        c.setDni("12345678");
                        c.setSexo(Sexo.MASCULINO);
                        c.setTipoIva(TipoIva.CONSUMIDOR_FINAL);
                        c.setProductor(productor);
                        c.setActivo(true);
                        return clienteRepository.save(c);
                    });

            Cliente c2 = clienteRepository.findAll().stream()
                    .filter(c -> "87654321".equals(c.getDni()))
                    .findFirst()
                    .orElseGet(() -> {
                        Cliente c = new Cliente();
                        c.setNombre("Maria");
                        c.setApellido("Gomez");
                        c.setFechaNacimiento(java.time.LocalDate.of(1990, 8, 22));
                        c.setDireccion("San Martin 456");
                        c.setLocalidad(localidad);
                        c.setTelefono("2910987654");
                        c.setEmail("maria.gomez@example.com");
                        c.setDni("87654321");
                        c.setSexo(Sexo.FEMENINO);
                        c.setTipoIva(TipoIva.MONOTRIBUTISTA);
                        c.setProductor(productor);
                        c.setActivo(true);
                        return clienteRepository.save(c);
                    });

            Cliente c3 = clienteRepository.findAll().stream()
                    .filter(c -> "11223344".equals(c.getDni()))
                    .findFirst()
                    .orElseGet(() -> {
                        Cliente c = new Cliente();
                        c.setNombre("Carlos");
                        c.setApellido("Rodriguez");
                        c.setFechaNacimiento(java.time.LocalDate.of(1978, 12, 5));
                        c.setDireccion("O'Higgins 789");
                        c.setLocalidad(localidad);
                        c.setTelefono("2915554433");
                        c.setEmail("carlos.rodriguez@example.com");
                        c.setDni("11223344");
                        c.setSexo(Sexo.MASCULINO);
                        c.setTipoIva(TipoIva.RESPONSABLE_INSCRIPTO);
                        c.setProductor(productor);
                        c.setActivo(true);
                        return clienteRepository.save(c);
                    });

            if (polizaRepository.count() == 0) {
                List<Compania> companias = companiaRepository.findAll();
                List<Ramo> ramos = ramoRepository.findAll();
                
                Compania comp1 = companias.isEmpty() ? null : companias.get(0);
                Compania comp2 = companias.size() > 1 ? companias.get(1) : comp1;
                
                Ramo ramoAutomotor = ramos.stream().filter(r -> r.getNombre().equalsIgnoreCase("Automotor")).findFirst().orElse(ramos.isEmpty() ? null : ramos.get(0));
                Ramo ramoHogar = ramos.stream().filter(r -> r.getNombre().equalsIgnoreCase("Combinado Familiar")).findFirst().orElse(ramos.size() > 1 ? ramos.get(1) : ramoAutomotor);

                Poliza p1 = new Poliza();
                p1.setNroPza("POL-AUTO-001");
                p1.setTomador(c1);
                p1.setTipoPago(TipoPago.TARJETA_CREDITO);
                p1.setProductor(productor);
                p1.setInicioVigencia(java.time.LocalDate.now().minusMonths(3));
                p1.setFinVigencia(java.time.LocalDate.now().plusMonths(9));
                p1.setRamo(ramoAutomotor);
                p1.setCompania(comp1);
                p1.setTipoFacturacion("Mensual");
                p1.setPrima(15000.0);
                p1.setPremio(18000.0);
                p1.setActivo(true);

                Poliza p2 = new Poliza();
                p2.setNroPza("POL-HOGAR-002");
                p2.setTomador(c2);
                p2.setTipoPago(TipoPago.EFECTIVO);
                p2.setProductor(productor);
                p2.setInicioVigencia(java.time.LocalDate.now().minusMonths(1));
                p2.setFinVigencia(java.time.LocalDate.now().plusMonths(11));
                p2.setRamo(ramoHogar);
                p2.setCompania(comp2);
                p2.setTipoFacturacion("Mensual");
                p2.setPrima(8000.0);
                p2.setPremio(9500.0);
                p2.setActivo(true);

                Poliza p3 = new Poliza();
                p3.setNroPza("POL-AUTO-003");
                p3.setTomador(c3);
                p3.setTipoPago(TipoPago.TARJETA_DEBITO);
                p3.setProductor(productor);
                p3.setInicioVigencia(java.time.LocalDate.now().minusDays(15));
                p3.setFinVigencia(java.time.LocalDate.now().plusDays(350));
                p3.setRamo(ramoAutomotor);
                p3.setCompania(comp1);
                p3.setTipoFacturacion("Semestral");
                p3.setPrima(20000.0);
                p3.setPremio(24000.0);
                p3.setActivo(true);

                polizaRepository.saveAll(List.of(p1, p2, p3));
            }
        }
    }
}