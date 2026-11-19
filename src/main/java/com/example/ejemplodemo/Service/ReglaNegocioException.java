package com.example.ejemplodemo.Service;

// Regla de negocio incumplida. Si indica un campo, el formulario muestra el mensaje junto a ese campo
public class ReglaNegocioException extends IllegalArgumentException {

    private final String campo;

    public ReglaNegocioException(String mensaje) {
        this(null, mensaje);
    }

    public ReglaNegocioException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    // Nombre de la propiedad del formulario afectada (null si es un error general)
    public String getCampo() {
        return campo;
    }
}
