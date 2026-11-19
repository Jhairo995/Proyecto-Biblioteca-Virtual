package com.example.ejemplodemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "libros")
@Getter
@Setter
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String categoria;

    private String idioma;

    // GRATUITO: lo leen todos los lectores; PREMIUM: solo lectores con cuenta Premium
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private NivelCuenta nivelAcceso = NivelCuenta.GRATUITO;

    // Todo libro subido por un escritor queda PENDIENTE hasta que un administrador lo revise
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private EstadoLibro estado = EstadoLibro.PENDIENTE;

    // Nombre del archivo almacenado en disco
    private String archivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escritor_id")
    private Usuario escritor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // Clase CSS de la portada según la categoría (no se persiste: la entidad usa acceso por campo)
    public String getPortada() {
        return switch (categoria == null ? "" : categoria) {
            case "Desarrollo Web" -> "cover-blue";
            case "Java" -> "cover-orange";
            case "Bases de Datos" -> "cover-dark";
            case "IA y Machine Learning" -> "cover-pink";
            default -> "cover-cyan";
        };
    }
}
