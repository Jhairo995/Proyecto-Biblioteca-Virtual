package com.example.ejemplodemo.model;

public enum Rol {
    LECTOR("/lector"),
    ESCRITOR("/escritor"),
    ADMINISTRADOR("/admin");

    // Ruta del panel al que se redirige al usuario tras iniciar sesión
    private final String panel;

    Rol(String panel) {
        this.panel = panel;
    }

    public String getPanel() {
        return panel;
    }
}
