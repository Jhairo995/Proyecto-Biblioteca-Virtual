package com.example.ejemplodemo.Repository;

import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    boolean existsByDniAndIdNot(String dni, Long id);

    long countByRol(Rol rol);

    List<Usuario> findByRol(Rol rol);

    long countByActivoTrue();

    List<Usuario> findByRol_AccesoAndActivoTrueOrderByNombreAsc(TipoAcceso acceso);

    // Administradores activos sin contar a un usuario (para no dejar el sistema sin administradores)
    @Query("select count(u) from Usuario u where u.activo = true and u.rol.acceso = :acceso and u.id <> :usuarioId")
    long contarActivosPorAccesoExcepto(@Param("acceso") TipoAcceso acceso, @Param("usuarioId") Long usuarioId);

    // Usuarios activos con ese acceso que pertenecen a otros roles
    @Query("select count(u) from Usuario u where u.activo = true and u.rol.acceso = :acceso and u.rol <> :rol")
    long contarActivosPorAccesoFueraDeRol(@Param("acceso") TipoAcceso acceso, @Param("rol") Rol rol);

    @Query("select u.rol.id as id, count(u) as total from Usuario u group by u.rol.id")
    List<ConteoPorId> contarPorRol();

    // Búsqueda del panel: cada filtro se ignora cuando llega nulo
    @Query("""
            select u from Usuario u
            where (:texto is null or lower(u.nombre) like :texto or lower(u.correo) like :texto or u.dni like :texto)
              and (:rolId is null or u.rol.id = :rolId)
              and (:activo is null or u.activo = :activo)
            """)
    Page<Usuario> buscar(@Param("texto") String texto,
                         @Param("rolId") Long rolId,
                         @Param("activo") Boolean activo,
                         Pageable pageable);
}
