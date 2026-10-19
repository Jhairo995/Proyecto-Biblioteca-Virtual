package com.example.ejemplodemo.Repository;

import com.example.ejemplodemo.model.Lectura;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturaRepository extends JpaRepository<Lectura, Long> {
    List<Lectura> findByLectorOrderByFechaAgregadoDesc(Usuario lector);

    boolean existsByLectorAndLibro(Usuario lector, Libro libro);

    void deleteByLectorAndLibro(Usuario lector, Libro libro);
}
