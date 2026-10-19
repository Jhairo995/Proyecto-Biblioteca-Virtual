package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.LibroRepository;
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

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final AlmacenamientoService almacenamientoService;

    public LibroService(LibroRepository libroRepository, AlmacenamientoService almacenamientoService) {
        this.libroRepository = libroRepository;
        this.almacenamientoService = almacenamientoService;
    }

    // Datos que el escritor envía desde el formulario de publicación
    public record DatosLibro(String titulo, String descripcion, String categoria, String idioma, NivelCuenta nivelAcceso) {
    }

    // El libro queda PENDIENTE hasta que un administrador lo revise
    public Libro registrarLibro(DatosLibro datos, MultipartFile archivo, Usuario escritor) {
        if (datos.titulo() == null || datos.titulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio.");
        }
        if (datos.categoria() == null || datos.categoria().isBlank()) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }

        Libro libro = new Libro();
        libro.setTitulo(datos.titulo().trim());
        libro.setDescripcion(datos.descripcion() == null ? null : datos.descripcion().trim());
        libro.setCategoria(datos.categoria().trim());
        libro.setIdioma(datos.idioma() == null ? null : datos.idioma().trim());
        libro.setNivelAcceso(datos.nivelAcceso() == null ? NivelCuenta.GRATUITO : datos.nivelAcceso());
        libro.setEstado(EstadoLibro.PENDIENTE);
        libro.setEscritor(escritor);
        libro.setArchivo(almacenamientoService.guardarPdf(archivo));

        return libroRepository.save(libro);
    }

    public List<Libro> librosDeEscritor(Usuario escritor) {
        return libroRepository.findByEscritorOrderByFechaRegistroDesc(escritor);
    }

    public List<Libro> librosPorEstado(EstadoLibro estado) {
        return libroRepository.findByEstadoOrderByFechaRegistroDesc(estado);
    }

    public long contarPorEstado(EstadoLibro estado) {
        return libroRepository.countByEstado(estado);
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
