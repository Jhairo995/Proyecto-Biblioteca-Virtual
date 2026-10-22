package com.example.ejemplodemo.Repository;

import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.ResumenAutor;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// Corre contra la base de datos configurada (MySQL/XAMPP) y cada test se revierte al terminar.
// ddl-auto=update evita que el test pueda borrar tablas si cambiara el valor por defecto.
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=update")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ConsultasAutoresTest {

    // Prefijo para distinguir los datos del test de los que ya existen en la base
    private static final String PREFIJO = "ZZ Test Autores ";
    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    @Autowired
    private TestEntityManager em;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private LibroRepository libroRepository;

    private Usuario usuario(String nombre, Rol rol) {
        int n = SECUENCIA.incrementAndGet();
        Usuario usuario = new Usuario();
        usuario.setNombre(PREFIJO + nombre);
        usuario.setCorreo("autores" + n + "-" + System.nanoTime() + "@test.local");
        usuario.setDni(String.format("8%07d", (System.nanoTime() + n) % 10_000_000));
        usuario.setContrasena("x");
        usuario.setRol(rol);
        return em.persist(usuario);
    }

    private Libro libro(Usuario escritor, EstadoLibro estado, long vistas) {
        Libro libro = new Libro();
        libro.setTitulo("Libro de " + escritor.getNombre());
        libro.setCategoria("Java");
        libro.setEstado(estado);
        libro.setEscritor(escritor);
        libro.setVistas(vistas);
        return em.persist(libro);
    }

    private List<ResumenAutor> autoresDelTest() {
        em.flush();
        return usuarioRepository.resumenAutores(Rol.ESCRITOR, EstadoLibro.APROBADO).stream()
                .filter(autor -> autor.nombre().startsWith(PREFIJO))
                .toList();
    }

    @Test
    void cuentaSoloLosLibrosPublicadosYSumaSusVistas() {
        Usuario ana = usuario("Ana", Rol.ESCRITOR);
        libro(ana, EstadoLibro.APROBADO, 5);
        libro(ana, EstadoLibro.APROBADO, 7);
        libro(ana, EstadoLibro.PENDIENTE, 100);
        libro(ana, EstadoLibro.RECHAZADO, 50);

        assertThat(autoresDelTest()).containsExactly(new ResumenAutor(PREFIJO + "Ana", 2L, 12L));
    }

    @Test
    void incluyeEscritoresSinLibrosPublicadosConCeros() {
        Usuario beto = usuario("Beto", Rol.ESCRITOR);
        libro(beto, EstadoLibro.PENDIENTE, 0);

        assertThat(autoresDelTest()).containsExactly(new ResumenAutor(PREFIJO + "Beto", 0L, 0L));
    }

    @Test
    void soloListaUsuariosConRolEscritor() {
        usuario("Lectora", Rol.LECTOR);
        usuario("Admin", Rol.ADMINISTRADOR);
        usuario("Carla", Rol.ESCRITOR);

        assertThat(autoresDelTest()).extracting(ResumenAutor::nombre).containsExactly(PREFIJO + "Carla");
    }

    @Test
    void ordenaPorLibrosPublicadosLuegoPorVistasYLuegoPorNombre() {
        Usuario pocasVistas = usuario("B pocas vistas", Rol.ESCRITOR);
        libro(pocasVistas, EstadoLibro.APROBADO, 1);
        Usuario muchasVistas = usuario("C muchas vistas", Rol.ESCRITOR);
        libro(muchasVistas, EstadoLibro.APROBADO, 90);
        Usuario masLibros = usuario("D mas libros", Rol.ESCRITOR);
        libro(masLibros, EstadoLibro.APROBADO, 0);
        libro(masLibros, EstadoLibro.APROBADO, 0);
        Usuario empateA = usuario("A empate", Rol.ESCRITOR);
        libro(empateA, EstadoLibro.APROBADO, 1);

        assertThat(autoresDelTest()).extracting(ResumenAutor::nombre).containsExactly(
                PREFIJO + "D mas libros",
                PREFIJO + "C muchas vistas",
                PREFIJO + "A empate",
                PREFIJO + "B pocas vistas");
    }

    @Test
    void sumarVistaIncrementaElContadorEnLaBaseDeDatos() {
        Libro libro = libro(usuario("Eva", Rol.ESCRITOR), EstadoLibro.APROBADO, 3);
        em.flush();

        libroRepository.sumarVista(libro.getId());
        libroRepository.sumarVista(libro.getId());
        em.clear();

        assertThat(em.find(Libro.class, libro.getId()).getVistas()).isEqualTo(5);
    }
}
