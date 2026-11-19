package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.dto.RegistroUsuario;
import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.Service.UsuarioService;
import org.springframework.dao.DataIntegrityViolationException;
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
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroUsuario datos) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(datos);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Usuario registrado exitosamente",
                    "id", nuevoUsuario.getId(),
                    "nombre", nuevoUsuario.getNombre(),
                    "correo", nuevoUsuario.getCorreo(),
                    "rol", nuevoUsuario.getAcceso().name(),
                    "nivelCuenta", nuevoUsuario.getNivelCuenta().name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            // Dos registros simultáneos con el mismo correo o DNI: la BD rechaza el duplicado
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "El correo o el DNI ya están registrados en el sistema."));
        }
    }
}