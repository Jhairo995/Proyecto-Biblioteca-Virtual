package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.CategoriaRepository;
import com.example.ejemplodemo.Repository.ConteoPorId;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.dto.CategoriaForm;
import com.example.ejemplodemo.model.Categoria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private static final Pattern PATRON_ICONO = Pattern.compile("^bi-[a-z0-9]+(-[a-z0-9]+)*$");

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, LibroRepository libroRepository) {
        this.categoriaRepository = categoriaRepository;
        this.libroRepository = libroRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivaTrueOrderByNombreAsc();
    }

    public Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada."));
    }

    public Map<Long, Long> librosPorCategoria() {
        return libroRepository.contarPorCategoria().stream()
                .filter(conteo -> conteo.getId() != null)
                .collect(Collectors.toUnmodifiableMap(ConteoPorId::getId, ConteoPorId::getTotal));
    }

    // Categoría elegida para un libro: debe existir y estar activa (salvo que el libro ya la tuviera)
    public Categoria paraLibro(Long id, Categoria actual) {
        if (id == null) {
            throw new ReglaNegocioException("categoriaId", "Selecciona una categoría.");
        }
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("categoriaId", "La categoría seleccionada no existe."));
        boolean esLaActual = actual != null && Objects.equals(actual.getId(), categoria.getId());
        if (!categoria.isActiva() && !esLaActual) {
            throw new ReglaNegocioException("categoriaId",
                    "La categoría «" + categoria.getNombre() + "» está desactivada.");
        }
        return categoria;
    }

    @Transactional
    public Categoria crear(CategoriaForm form) {
        String nombre = validar(form);
        validarNombreLibre(nombre, null);
        Categoria categoria = new Categoria();
        aplicar(categoria, nombre, form);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaForm form) {
        Categoria categoria = buscar(id);
        String nombre = validar(form);
        validarNombreLibre(nombre, id);
        aplicar(categoria, nombre, form);
        return categoria;
    }

    @Transactional
    public Categoria eliminar(Long id) {
        Categoria categoria = buscar(id);
        long libros = libroRepository.countByCategoria(categoria);
        if (libros > 0) {
            throw new ReglaNegocioException("No se puede eliminar «" + categoria.getNombre() + "»: tiene " + libros
                    + " libro(s). Muévelos a otra categoría o desactívala para ocultarla.");
        }
        categoriaRepository.delete(categoria);
        return categoria;
    }

    // Valida los datos y devuelve el nombre normalizado
    private static String validar(CategoriaForm form) {
        String nombre = Textos.obligatorio(form.getNombre(), "nombre", "El nombre de la categoría es obligatorio.");
        Textos.longitudMaxima(nombre, Categoria.NOMBRE_MAX, "nombre", "El nombre admite como máximo 60 caracteres.");
        Textos.longitudMaxima(form.getDescripcion(), Categoria.DESCRIPCION_MAX, "descripcion",
                "La descripción admite como máximo 255 caracteres.");
        String icono = Textos.obligatorio(form.getIcono(), "icono", "Indica el ícono de la categoría.");
        if (icono.length() > Categoria.ICONO_MAX || !PATRON_ICONO.matcher(icono).matches()) {
            throw new ReglaNegocioException("icono", "Usa el nombre de un ícono de Bootstrap Icons, por ejemplo: bi-database.");
        }
        if (form.getColor() == null) {
            throw new ReglaNegocioException("color", "Selecciona el color de portada.");
        }
        return nombre;
    }

    private void validarNombreLibre(String nombre, Long id) {
        boolean ocupado = id == null
                ? categoriaRepository.existsByNombreIgnoreCase(nombre)
                : categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (ocupado) {
            throw new ReglaNegocioException("nombre", "Ya existe una categoría con el nombre «" + nombre + "».");
        }
    }

    private static void aplicar(Categoria categoria, String nombre, CategoriaForm form) {
        categoria.setNombre(nombre);
        categoria.setDescripcion(Textos.limpiar(form.getDescripcion()));
        categoria.setIcono(form.getIcono().trim());
        categoria.setColor(form.getColor());
        categoria.setActiva(form.isActiva());
    }
}
