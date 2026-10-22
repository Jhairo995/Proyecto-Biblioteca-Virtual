package com.example.ejemplodemo.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

// Fila de la sección Autores: un escritor con sus libros publicados y la suma de sus vistas
// (lo arma UsuarioRepository.resumenAutores directamente desde la consulta)
public record ResumenAutor(String nombre, Long librosPublicados, Long vistas) {

    private static final int MAXIMO_INICIALES = 2;

    // Iniciales para el avatar: primera letra de las dos primeras palabras del nombre
    public String iniciales() {
        String iniciales = Arrays.stream(nombre.trim().split("\\s+"))
                .filter(palabra -> !palabra.isEmpty())
                .limit(MAXIMO_INICIALES)
                .map(palabra -> palabra.substring(0, 1).toUpperCase(Locale.ROOT))
                .collect(Collectors.joining());
        return iniciales.isEmpty() ? "?" : iniciales;
    }
}
