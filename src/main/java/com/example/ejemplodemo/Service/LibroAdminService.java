package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.LecturaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.dto.LibroForm;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Gestión completa de libros desde el panel de administración
@Service
public class LibroAdminService {

    private final LibroRepository libroRepository;
    private final LecturaRepository lecturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final AlmacenamientoService almacenamientoService;

    public LibroAdminService(LibroRepository libroRepository,
                             LecturaRepository lecturaRepository,
                             UsuarioRepository usuarioRepository,
                             CategoriaService categoriaService,
                             AlmacenamientoService almacenamientoService) {
        this.libroRepository = libroRepository;
        this.lecturaRepository = lecturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
        this.almacenamientoService = almacenamientoService;
    }

    public Page<Libro> buscar(String texto, EstadoLibro estado, Long categoriaId, Pageable pageable) {
        return libroRepository.buscar(Textos.patronBusqueda(texto), estado, categoriaId, pageable);
    }

    public Libro buscar(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado."));
    }

    // Usuarios que pueden figurar como autores de un libro
    public List<Usuario> escritoresDisponibles() {
        return usuarioRepository.findByRol_AccesoAndActivoTrueOrderByNombreAsc(TipoAcceso.ESCRITOR);
    }

    @Transactional
    public Libro crear(LibroForm form, MultipartFile archivo) {
        Libro libro = new Libro();
        aplicar(libro, form);
        String nuevoArchivo = guardarPdf(archivo);
        libro.setArchivo(nuevoArchivo);
        eliminarArchivoAlTerminar(null, nuevoArchivo);
        return libroRepository.save(libro);
    }

    @Transactional
    public Libro actualizar(Long id, LibroForm form, MultipartFile archivo) {
        Libro libro = buscar(id);
        aplicar(libro, form);
        if (archivo != null && !archivo.isEmpty()) {
            String anterior = libro.getArchivo();
            String nuevoArchivo = guardarPdf(archivo);
            libro.setArchivo(nuevoArchivo);
            eliminarArchivoAlTerminar(anterior, nuevoArchivo);
        }
        return libro;
    }

    @Transactional
    public Libro eliminar(Long id) {
        Libro libro = buscar(id);
        lecturaRepository.deleteByLibro(libro);
        libroRepository.delete(libro);
        eliminarArchivoAlTerminar(libro.getArchivo(), null);
        return libro;
    }

    private void aplicar(Libro libro, LibroForm form) {
        String titulo = Textos.obligatorio(form.getTitulo(), "titulo", "El título es obligatorio.");
        Textos.longitudMaxima(titulo, LibroForm.TITULO_MAX, "titulo", "El título admite como máximo 200 caracteres.");
        Textos.longitudMaxima(form.getDescripcion(), LibroForm.DESCRIPCION_MAX, "descripcion",
                "La descripción admite como máximo 1000 caracteres.");
        Textos.longitudMaxima(form.getIdioma(), LibroForm.IDIOMA_MAX, "idioma", "El idioma admite como máximo 50 caracteres.");
        Textos.formato(form.getIdioma(), LibroService.PATRON_IDIOMA, "idioma", LibroService.MENSAJE_IDIOMA);
        if (form.getEstado() == null) {
            throw new ReglaNegocioException("estado", "Selecciona el estado del libro.");
        }

        libro.setCategoria(categoriaService.paraLibro(form.getCategoriaId(), libro.getCategoria()));
        libro.setEscritor(escritorPara(form.getEscritorId(), libro.getEscritor()));
        libro.setTitulo(titulo);
        libro.setDescripcion(Textos.limpiar(form.getDescripcion()));
        libro.setIdioma(Textos.limpiar(form.getIdioma()));
        libro.setNivelAcceso(form.getNivelAcceso() == null ? NivelCuenta.GRATUITO : form.getNivelAcceso());
        libro.setEstado(form.getEstado());
    }

    // El autor debe ser un escritor activo; se conserva el actual aunque ya no lo sea
    private Usuario escritorPara(Long id, Usuario actual) {
        if (id == null) {
            return null;
        }
        if (actual != null && id.equals(actual.getId())) {
            return actual;
        }
        Usuario escritor = usuarioRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("escritorId", "El escritor seleccionado no existe."));
        if (escritor.getAcceso() != TipoAcceso.ESCRITOR || !escritor.isActivo()) {
            throw new ReglaNegocioException("escritorId",
                    "«" + escritor.getNombre() + "» no tiene una cuenta de escritor activa.");
        }
        return escritor;
    }

    private String guardarPdf(MultipartFile archivo) {
        try {
            return almacenamientoService.guardarPdf(archivo);
        } catch (ReglaNegocioException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("archivo", e.getMessage());
        }
    }

    // Borra del disco el PDF que sobra según termine la transacción: el anterior si se confirma, el nuevo si se revierte
    private void eliminarArchivoAlTerminar(String siConfirma, String siRevierte) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            almacenamientoService.eliminar(siConfirma);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int estado) {
                almacenamientoService.eliminar(estado == STATUS_COMMITTED ? siConfirma : siRevierte);
            }
        });
    }
}
