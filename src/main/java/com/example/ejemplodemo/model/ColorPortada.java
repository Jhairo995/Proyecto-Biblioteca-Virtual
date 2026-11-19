package com.example.ejemplodemo.model;

// Colores de portada disponibles para las categorías (clases CSS definidas en estilo.css)
public enum ColorPortada {
    AZUL("cover-blue", "Azul"),
    VERDE("cover-green", "Verde"),
    OSCURO("cover-dark", "Oscuro"),
    NARANJA("cover-orange", "Naranja"),
    CIAN("cover-cyan", "Cian"),
    ROSA("cover-pink", "Rosa");

    private final String clase;
    private final String etiqueta;

    ColorPortada(String clase, String etiqueta) {
        this.clase = clase;
        this.etiqueta = etiqueta;
    }

    public String getClase() {
        return clase;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    // Color que tenían las categorías originales (antes eran texto libre en el libro)
    public static ColorPortada sugeridoPara(String nombreCategoria) {
        return switch (nombreCategoria == null ? "" : nombreCategoria.trim()) {
            case "Desarrollo Web" -> AZUL;
            case "Java" -> NARANJA;
            case "Bases de Datos" -> OSCURO;
            case "IA y Machine Learning" -> ROSA;
            default -> CIAN;
        };
    }
}
