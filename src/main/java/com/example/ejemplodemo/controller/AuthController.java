package com.example.ejemplodemo.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/registro")
    public Map<String, String> registrarUsuario() {
        return Map.of("mensaje", "Endpoint listo para recibir datos de registro.");
    }

    @PostMapping("/login")
    public Map<String, String> iniciarSesion() {
        return Map.of("mensaje", "Endpoint listo para validar credenciales temporales.");
    }
}