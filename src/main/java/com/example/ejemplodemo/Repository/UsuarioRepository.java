package com.example.ejemplodemo.Repository;

import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.ResumenAutor;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByDni(String dni);

    // Usuarios con el rol indicado, con cuántos libros tienen en ese estado y la suma de sus vistas.
    // LEFT JOIN: también aparecen quienes aún no tienen libros (con 0 y 0)
    @Query("""
            select new com.example.ejemplodemo.model.ResumenAutor(u.nombre, count(l), coalesce(sum(l.vistas), 0L))
            from Usuario u
            left join Libro l on l.escritor = u and l.estado = :estado
            where u.rol = :rol
            group by u.id, u.nombre
            order by count(l) desc, coalesce(sum(l.vistas), 0L) desc, u.nombre
            """)
    List<ResumenAutor> resumenAutores(@Param("rol") Rol rol, @Param("estado") EstadoLibro estado);
}