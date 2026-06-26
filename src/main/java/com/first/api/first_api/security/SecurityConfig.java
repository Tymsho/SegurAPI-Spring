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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin
                                                                                                              // estado
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado o token inválido/expirado");
                }))
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
                        .requestMatchers("/api/pagos/webhook").permitAll()
                        .requestMatchers("/ws/**").permitAll() // Permitir Handshake de WebSockets

                        // 2. Gestión de Usuarios: Exclusivo para el Administrador
                        // Esto impide que un productor liste, modifique o penalice a otros usuarios
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // 3. Operaciones Comerciales: Tanto PRODUCTOR como ADMIN pueden operar
                        // Permitimos que el productor tenga control total (Lectura, Creación y Edición)
                        // sobre su cartera
                        .requestMatchers("/api/clientes/**", "/api/polizas/**").hasAnyRole("PRODUCTOR", "ADMIN")
                        
                        // Localidades (Público para selectores)
                        .requestMatchers("/api/localidades/**").permitAll()

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Configurar el origen de Vite (por defecto corre en el puerto 5173)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicar esta configuración de CORS a todos los endpoints de la API
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}