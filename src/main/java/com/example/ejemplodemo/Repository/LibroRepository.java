package com.example.ejemplodemo.Repository;

import com.example.ejemplodemo.model.Categoria;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByEstadoOrderByFechaRegistroDesc(EstadoLibro estado);

    List<Libro> findByEscritorOrderByFechaRegistroDesc(Usuario escritor);

    List<Libro> findTop10ByEstadoOrderByFechaRegistroDesc(EstadoLibro estado);

    long countByEstado(EstadoLibro estado);

    long countByCategoria(Categoria categoria);

    long countByEscritor(Usuario escritor);

    @Query("select l.categoria.id as id, count(l) as total from Libro l group by l.categoria.id")
    List<ConteoPorId> contarPorCategoria();

    // Búsqueda del panel: cada filtro se ignora cuando llega nulo
    @EntityGraph(attributePaths = {"categoria", "escritor"})
    @Query("""
            select l from Libro l
            where (:texto is null or lower(l.titulo) like :texto)
              and (:estado is null or l.estado = :estado)
              and (:categoriaId is null or l.categoria.id = :categoriaId)
            """)
    Page<Libro> buscar(@Param("texto") String texto,
                       @Param("estado") EstadoLibro estado,
                       @Param("categoriaId") Long categoriaId,
                       Pageable pageable);
}
