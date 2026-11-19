package com.example.ejemplodemo.Service;

import java.util.Locale;
import java.util.regex.Pattern;

// Normalización de textos recibidos de formularios
final class Textos {

    private Textos() {
    }

    // Quita espacios a los extremos; un texto vacío se trata como ausente
    static String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String recortado = valor.trim();
        return recortado.isEmpty() ? null : recortado;
    }

    static String obligatorio(String valor, String campo, String mensaje) {
        String limpio = limpiar(valor);
        if (limpio == null) {
            throw new ReglaNegocioException(campo, mensaje);
        }
        return limpio;
    }

    static void longitudMaxima(String valor, int maximo, String campo, String mensaje) {
        if (valor != null && valor.length() > maximo) {
            throw new ReglaNegocioException(campo, mensaje);
        }
    }

    // Un texto opcional que, si viene, debe cumplir el patrón
    static void formato(String valor, Pattern patron, String campo, String mensaje) {
        String limpio = limpiar(valor);
        if (limpio != null && !patron.matcher(limpio).matches()) {
            throw new ReglaNegocioException(campo, mensaje);
        }
    }

    // Patrón para "like" sin distinguir mayúsculas (null si no hay texto que buscar)
    static String patronBusqueda(String texto) {
        String limpio = limpiar(texto);
        return limpio == null ? null : "%" + limpio.toLowerCase(Locale.ROOT) + "%";
    }
}
