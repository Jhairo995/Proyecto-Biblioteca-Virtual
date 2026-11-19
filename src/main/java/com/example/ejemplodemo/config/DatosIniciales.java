package com.example.ejemplodemo.config;

import com.example.ejemplodemo.Repository.CategoriaRepository;
import com.example.ejemplodemo.Repository.RolRepository;
import com.example.ejemplodemo.model.Categoria;
import com.example.ejemplodemo.model.ColorPortada;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

// Crea los roles del sistema y las categorías iniciales, y migra las bases antiguas (antes que AdminInicializador)
@Component
@Order(1)
public class DatosIniciales implements CommandLineRunner {

    private static final String CATEGORIA_POR_DEFECTO = "Otros";

    private record RolBase(TipoAcceso acceso, String nombre, String descripcion) {
    }

    private record CategoriaBase(String nombre, String descripcion, String icono, ColorPortada color) {
    }

    private static final List<RolBase> ROLES = List.of(
            new RolBase(TipoAcceso.LECTOR, "Lector",
                    "Lee los libros del catálogo y administra su lista de lectura."),
            new RolBase(TipoAcceso.ESCRITOR, "Escritor",
                    "Publica libros que pasan por revisión antes de aparecer en el catálogo."),
            new RolBase(TipoAcceso.ADMINISTRADOR, "Administrador",
                    "Gestiona usuarios, roles, categorías y libros de la plataforma."));

    private static final List<CategoriaBase> CATEGORIAS = List.of(
            new CategoriaBase("Desarrollo Web", "HTML, CSS, JavaScript, React, Node.js y más.", "bi-globe", ColorPortada.AZUL),
            new CategoriaBase("Java", "Programación orientada a objetos, Spring y desarrollo empresarial.",
                    "bi-filetype-java", ColorPortada.NARANJA),
            new CategoriaBase("Bases de Datos", "MySQL, PostgreSQL, SQL Server, MongoDB y más.", "bi-database", ColorPortada.OSCURO),
            new CategoriaBase("IA y Machine Learning", "Python, inteligencia artificial, datos y aprendizaje automático.",
                    "bi-robot", ColorPortada.ROSA),
            new CategoriaBase(CATEGORIA_POR_DEFECTO, "Recursos de programación y tecnología en general.", "bi-grid",
                    ColorPortada.CIAN));

    private final RolRepository rolRepository;
    private final CategoriaRepository categoriaRepository;
    private final MigracionEsquemaLegado migracion;

    public DatosIniciales(RolRepository rolRepository,
                          CategoriaRepository categoriaRepository,
                          MigracionEsquemaLegado migracion) {
        this.rolRepository = rolRepository;
        this.categoriaRepository = categoriaRepository;
        this.migracion = migracion;
    }

    @Override
    public void run(String... args) {
        ROLES.forEach(this::asegurarRol);
        migracion.migrarRolesDeUsuarios(rolRepository.findFirstByAccesoAndSistemaTrue(TipoAcceso.LECTOR)
                .orElseThrow().getId());

        // Las categorías de ejemplo solo se crean cuando la tabla está vacía (primer arranque)
        if (categoriaRepository.count() == 0) {
            CATEGORIAS.forEach(base -> crearCategoria(base.nombre(), base.descripcion(), base.icono(), base.color()));
        }
        migracion.migrarCategoriasDeLibros(this::crearCategoriaLegada, this::idCategoriaPorDefecto);
    }

    // Solo se usa si al migrar quedan libros sin una categoría reconocida
    private Long idCategoriaPorDefecto() {
        return categoriaRepository.findByNombreIgnoreCase(CATEGORIA_POR_DEFECTO)
                .orElseGet(() -> crearCategoria(CATEGORIA_POR_DEFECTO, null, "bi-grid", ColorPortada.CIAN))
                .getId();
    }

    // Si ya hay un rol con ese nombre (creado a mano) se convierte en el rol del sistema
    private void asegurarRol(RolBase base) {
        if (rolRepository.findFirstByAccesoAndSistemaTrue(base.acceso()).isPresent()) {
            return;
        }
        Rol rol = rolRepository.findByNombreIgnoreCase(base.nombre()).orElseGet(Rol::new);
        rol.setNombre(base.nombre());
        if (rol.getDescripcion() == null) {
            rol.setDescripcion(base.descripcion());
        }
        rol.setAcceso(base.acceso());
        rol.setSistema(true);
        rolRepository.save(rol);
    }

    private Categoria crearCategoria(String nombre, String descripcion, String icono, ColorPortada color) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setIcono(icono);
        categoria.setColor(color);
        categoria.setActiva(true);
        return categoriaRepository.save(categoria);
    }

    // Categorías que existían como texto en los libros antiguos
    private void crearCategoriaLegada(String nombre) {
        if (nombre.length() <= Categoria.NOMBRE_MAX && !categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            crearCategoria(nombre, null, Categoria.ICONO_POR_DEFECTO, ColorPortada.sugeridoPara(nombre));
        }
    }
}
