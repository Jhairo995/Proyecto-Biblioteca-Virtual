package com.example.ejemplodemo.security;

import com.example.ejemplodemo.model.Rol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private static final String PREFIJO_ROL = "ROLE_";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivado temporalmente para permitir peticiones POST desde formularios y APIs sin tokens CSRF
            .csrf(csrf -> csrf.disable())
            // Permite incrustar el PDF del libro en un iframe del mismo sitio (visor del lector)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: Vistas web principales y archivos estáticos (CSS, JS, imágenes)
                .requestMatchers("/", "/login", "/catalogo", "/categorias", "/suscripcion", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Rutas públicas de API: Registro y datos de inicio
                .requestMatchers("/api/auth/**", "/api/inicio/**").permitAll()
                // Paneles por rol
                .requestMatchers("/admin/**").hasRole(Rol.ADMINISTRADOR.name())
                .requestMatchers("/escritor/**").hasRole(Rol.ESCRITOR.name())
                .requestMatchers("/lector/**").hasRole(Rol.LECTOR.name())
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("contrasena")
                .successHandler(redireccionPorRol())
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    // Tras iniciar sesión, cada rol va a su propio panel (definido en el enum Rol)
    private AuthenticationSuccessHandler redireccionPorRol() {
        return (request, response, authentication) -> {
            String panel = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(autoridad -> autoridad.startsWith(PREFIJO_ROL))
                    .map(autoridad -> autoridad.substring(PREFIJO_ROL.length()))
                    .flatMap(nombre -> Arrays.stream(Rol.values()).filter(rol -> rol.name().equals(nombre)))
                    .map(Rol::getPanel)
                    .findFirst()
                    .orElse("/");
            response.sendRedirect(request.getContextPath() + panel);
        };
    }

    // Bean para cifrado irreversible de contraseñas con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
