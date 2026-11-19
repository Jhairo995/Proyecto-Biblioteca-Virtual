package com.example.ejemplodemo.security;

import com.example.ejemplodemo.model.TipoAcceso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private static final String PREFIJO_ROL = "ROLE_";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http
            // Desactivado temporalmente para permitir peticiones POST desde formularios y APIs sin tokens CSRF
            .csrf(csrf -> csrf.disable())
            // Permite incrustar el PDF del libro en un iframe del mismo sitio (visor del lector)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: Vistas web principales y archivos estáticos (CSS, JS, imágenes)
                .requestMatchers("/", "/login", "/catalogo", "/categorias", "/suscripcion", "/ejemplo-clase", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Rutas públicas de API: Registro y datos de inicio
                .requestMatchers("/api/auth/**", "/api/inicio/**").permitAll()
                // Paneles por rol
                .requestMatchers("/admin/**").hasRole(TipoAcceso.ADMINISTRADOR.name())
                .requestMatchers("/escritor/**").hasRole(TipoAcceso.ESCRITOR.name())
                .requestMatchers("/lector/**").hasRole(TipoAcceso.LECTOR.name())
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
            )
            // Se registran las sesiones para poder cerrarlas cuando el administrador cambia el acceso de un usuario
            .sessionManagement(sesion -> sesion
                .maximumSessions(-1)
                .sessionRegistry(sessionRegistry)
                .expiredUrl("/login?expirada")
            );

        return http.build();
    }

    // Tras iniciar sesión, cada rol va a su propio panel (definido en el enum TipoAcceso)
    private AuthenticationSuccessHandler redireccionPorRol() {
        return (request, response, authentication) -> {
            String panel = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(autoridad -> autoridad.startsWith(PREFIJO_ROL))
                    .map(autoridad -> autoridad.substring(PREFIJO_ROL.length()))
                    .flatMap(nombre -> Arrays.stream(TipoAcceso.values()).filter(acceso -> acceso.name().equals(nombre)))
                    .map(TipoAcceso::getPanel)
                    .findFirst()
                    .orElse("/");
            response.sendRedirect(request.getContextPath() + panel);
        };
    }

    // Registro de las sesiones activas (lo usa SesionesUsuario)
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // Avisa al registro cuando una sesión termina, para no acumular sesiones vencidas
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // Bean para cifrado irreversible de contraseñas con BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
