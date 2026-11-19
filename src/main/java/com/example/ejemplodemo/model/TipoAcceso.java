package com.example.ejemplodemo.model;

// Nivel de acceso que otorga un rol: define el panel y la autoridad de Spring Security (ROLE_<nombre>)
public enum TipoAcceso {
    LECTOR("/lector", "Lector"),
    ESCRITOR("/escritor", "Escritor"),
    ADMINISTRADOR("/admin", "Administrador");

    // Ruta del panel al que se redirige al usuario tras iniciar sesión
    private final String panel;
    private final String etiqueta;

    TipoAcceso(String panel, String etiqueta) {
        this.panel = panel;
        this.etiqueta = etiqueta;
    }

    public String getPanel() {
        return panel;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
