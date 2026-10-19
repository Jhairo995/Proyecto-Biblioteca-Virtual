package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

// Expone "usuarioActual" a todas las vistas Thymeleaf (null si nadie inició sesión)
@ControllerAdvice(annotations = Controller.class)
public class UsuarioActualAdvice {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return usuarioRepository.findByCorreo(principal.getName()).orElse(null);
    }
}
