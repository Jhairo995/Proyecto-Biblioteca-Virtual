package com.example.ejemplodemo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Libro que un lector agregó a su lista de lectura
@Entity
@Table(name = "lecturas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lector_id", "libro_id"}))
@Getter
@Setter
public class Lectura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lector_id")
    private Usuario lector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id")
    private Libro libro;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAgregado = LocalDateTime.now();
}
