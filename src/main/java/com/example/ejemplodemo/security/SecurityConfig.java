package com.example.ejemplodemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivado temporalmente para permitir peticiones POST desde formularios y APIs sin tokens CSRF
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: Vistas web principales y archivos estáticos (CSS, JS, imágenes)
                .requestMatchers("/", "/login", "/admin", "/css/**", "/js/**", "/images/**").permitAll()
                // Rutas públicas de API: Registro, login y datos de inicio
                .requestMatchers("/api/auth/**", "/api/inicio/**").permitAll()
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            );

        return http.build();
    }

    // Bean para cifrado irreversible de contraseñas con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}