package com.example.ejemplodemo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    // La columna admite NULL solo para poder migrar las bases antiguas (rol como texto); el servicio siempre asigna uno
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private NivelCuenta nivelCuenta = NivelCuenta.GRATUITO;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    // Una cuenta desactivada no puede iniciar sesión (alternativa segura a eliminarla)
    @ColumnDefault("true")
    @Column(nullable = false)
    private boolean activo = true;

    // Nulo en los usuarios creados antes de existir esta columna
    @Column(updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getDni() { return dni;}

    // Nivel de acceso efectivo del usuario (LECTOR si por algún motivo no tiene rol)
    public TipoAcceso getAcceso() {
        return rol == null ? TipoAcceso.LECTOR : rol.getAcceso();
    }

    // Iniciales para el avatar del panel
    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] partes = nombre.trim().split("\\s+");
        String iniciales = partes[0].substring(0, 1)
                + (partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "");
        return iniciales.toUpperCase();
    }
}
