package com.example.ejemplodemo.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/inicio")
public class InicioController {

    @GetMapping
    public Map<String, Object> obtenerMenuPrincipal() {
        return Map.of(
            "novedades", List.of("El Quijote", "Moby Dick"),
            "generos", List.of("Ficción", "Historia", "Ciencia"),
            "buscador_activo", true
        );
    }
}