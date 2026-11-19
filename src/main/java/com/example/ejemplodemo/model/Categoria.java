package com.example.ejemplodemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

// Categoría temática de los libros (administrada desde el panel)
@Entity
@Table(name = "categorias")
@Getter
@Setter
public class Categoria {
    public static final int NOMBRE_MAX = 60;
    public static final int DESCRIPCION_MAX = 255;
    public static final int ICONO_MAX = 40;
    public static final String ICONO_POR_DEFECTO = "bi-bookmark";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = NOMBRE_MAX)
    private String nombre;

    @Column(length = DESCRIPCION_MAX)
    private String descripcion;

    // Clase de Bootstrap Icons, por ejemplo "bi-database"
    @Column(nullable = false, length = ICONO_MAX)
    private String icono = ICONO_POR_DEFECTO;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ColorPortada color = ColorPortada.CIAN;

    // Las categorías inactivas no se ofrecen para libros nuevos ni se muestran en el sitio público
    @ColumnDefault("true")
    @Column(nullable = false)
    private boolean activa = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
