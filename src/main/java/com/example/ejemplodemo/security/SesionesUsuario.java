package com.example.ejemplodemo.security;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// Cierra las sesiones abiertas de un usuario cuando el administrador cambia su acceso, lo desactiva o lo elimina.
// Sin esto, el usuario conservaría sus permisos anteriores hasta que su sesión expirara.
@Component
public class SesionesUsuario {

    private final SessionRegistry sessionRegistry;

    public SesionesUsuario(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    // En su siguiente petición, el usuario vuelve al login (ver expiredUrl en SecurityConfig)
    public void cerrar(String correo) {
        if (correo == null) {
            return;
        }
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof UserDetails detalle
                        && detalle.getUsername().equalsIgnoreCase(correo))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .forEach(SessionInformation::expireNow);
    }
}
