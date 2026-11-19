package com.example.ejemplodemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

// Rol asignable a los usuarios. Cada rol otorga un nivel de acceso (lector, escritor o administrador)
@Entity
@Table(name = "roles")
@Getter
@Setter
public class Rol {
    public static final int NOMBRE_MAX = 50;
    public static final int DESCRIPCION_MAX = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = NOMBRE_MAX)
    private String nombre;

    @Column(length = DESCRIPCION_MAX)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoAcceso acceso = TipoAcceso.LECTOR;

    // Roles base creados por el sistema: no se eliminan ni cambian su nombre o nivel de acceso
    @Column(nullable = false)
    private boolean sistema;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Panel al que entra un usuario con este rol (no se persiste)
    public String getPanel() {
        return acceso.getPanel();
    }
}
