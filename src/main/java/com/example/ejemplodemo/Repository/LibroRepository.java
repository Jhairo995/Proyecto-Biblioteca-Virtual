package com.example.ejemplodemo.Repository;

import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByEstadoOrderByFechaRegistroDesc(EstadoLibro estado);

    List<Libro> findByEstadoAndCategoriaIgnoreCaseOrderByFechaRegistroDesc(EstadoLibro estado, String categoria);

    List<Libro> findByEscritorOrderByFechaRegistroDesc(Usuario escritor);

    long countByEstado(EstadoLibro estado);

    // Incremento atómico en la base de datos: dos lectores abriendo el libro a la vez no pierden vistas
    @Modifying
    @Query("update Libro l set l.vistas = l.vistas + 1 where l.id = :id")
    void sumarVista(@Param("id") Long id);
}
