package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Usuario registrado exitosamente",
                "id", nuevoUsuario.getId(),
                "nombre", nuevoUsuario.getNombre(),
                "correo", nuevoUsuario.getCorreo(),
                "nivelCuenta", nuevoUsuario.getNivelCuenta().name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public Map<String, String> iniciarSesion() {
        return Map.of("mensaje", "Endpoint listo para validar credenciales temporales.");
    }
}