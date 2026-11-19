package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.dto.LibroForm;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class LibroService {

    static final Pattern PATRON_IDIOMA = Pattern.compile(LibroForm.PATRON_IDIOMA);
    static final String MENSAJE_IDIOMA = "El idioma solo admite letras y espacios.";

    private final LibroRepository libroRepository;
    private final AlmacenamientoService almacenamientoService;
    private final CategoriaService categoriaService;

    public LibroService(LibroRepository libroRepository,
                        AlmacenamientoService almacenamientoService,
                        CategoriaService categoriaService) {
        this.libroRepository = libroRepository;
        this.almacenamientoService = almacenamientoService;
        this.categoriaService = categoriaService;
    }

    // Datos que el escritor envía desde el formulario de publicación
    public record DatosLibro(String titulo, String descripcion, Long categoriaId, String idioma, NivelCuenta nivelAcceso) {
    }

    // El libro queda PENDIENTE hasta que un administrador lo revise
    public Libro registrarLibro(DatosLibro datos, MultipartFile archivo, Usuario escritor) {
        String titulo = Textos.obligatorio(datos.titulo(), "titulo", "El título es obligatorio.");
        Textos.longitudMaxima(titulo, LibroForm.TITULO_MAX, "titulo", "El título admite como máximo 200 caracteres.");
        Textos.longitudMaxima(datos.descripcion(), LibroForm.DESCRIPCION_MAX, "descripcion",
                "La descripción admite como máximo 1000 caracteres.");
        Textos.longitudMaxima(datos.idioma(), LibroForm.IDIOMA_MAX, "idioma", "El idioma admite como máximo 50 caracteres.");
        Textos.formato(datos.idioma(), PATRON_IDIOMA, "idioma", MENSAJE_IDIOMA);
        if (datos.categoriaId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }

        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setDescripcion(Textos.limpiar(datos.descripcion()));
        libro.setCategoria(categoriaService.paraLibro(datos.categoriaId(), null));
        libro.setIdioma(Textos.limpiar(datos.idioma()));
        libro.setNivelAcceso(datos.nivelAcceso() == null ? NivelCuenta.GRATUITO : datos.nivelAcceso());
        libro.setEstado(EstadoLibro.PENDIENTE);
        libro.setEscritor(escritor);
        libro.setArchivo(almacenamientoService.guardarPdf(archivo));

        return libroRepository.save(libro);
    }

    public List<Libro> librosDeEscritor(Usuario escritor) {
        return libroRepository.findByEscritorOrderByFechaRegistroDesc(escritor);
    }

    // Los más recientes de un estado (resumen del panel)
    public List<Libro> ultimosPorEstado(EstadoLibro estado) {
        return libroRepository.findTop10ByEstadoOrderByFechaRegistroDesc(estado);
    }

    public List<Libro> librosPorEstado(EstadoLibro estado) {
        return libroRepository.findByEstadoOrderByFechaRegistroDesc(estado);
    }

    public long contarPorEstado(EstadoLibro estado) {
        return libroRepository.countByEstado(estado);
    }

    public long contarTodos() {
        return libroRepository.count();
    }

    public Libro buscar(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado."));
    }

    @Transactional
    public Libro cambiarEstado(Long id, EstadoLibro nuevoEstado) {
        Libro libro = buscar(id);
        libro.setEstado(nuevoEstado);
        return libro;
    }

    public Resource archivoDe(Libro libro) {
        return almacenamientoService.cargar(libro.getArchivo());
    }
}
