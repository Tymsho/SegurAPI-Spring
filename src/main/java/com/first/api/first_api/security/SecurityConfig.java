package com.first.api.first_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin
                                                                                                              // estado
                .authorizeHttpRequests(auth -> auth
                        // 1. Rutas públicas y documentación (Siempre al principio)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/usuarios/registro").permitAll()
                        .requestMatchers("/api/usuarios/verificar").permitAll()
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()

                        // 2. Gestión de Usuarios: Exclusivo para el Administrador
                        // Esto impide que un productor liste, modifique o penalice a otros usuarios
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // 3. Operaciones Comerciales: Tanto PRODUCTOR como ADMIN pueden operar
                        // Permitimos que el productor tenga control total (Lectura, Creación y Edición)
                        // sobre su cartera
                        .requestMatchers("/api/clientes/**", "/api/polizas/**").hasAnyRole("PRODUCTOR", "ADMIN")

                        // 4. Cualquier otra petición no especificada requiere autenticación
                        .anyRequest().authenticated());

        // Añadir el filtro JWT antes del filtro de usuario/contraseña por defecto
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Encriptador oficial de Spring
        return new BCryptPasswordEncoder();
    }
}