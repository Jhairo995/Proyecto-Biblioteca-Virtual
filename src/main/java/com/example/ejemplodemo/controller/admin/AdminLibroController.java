package com.example.ejemplodemo.controller.admin;

import com.example.ejemplodemo.Service.CategoriaService;
import com.example.ejemplodemo.Service.LibroAdminService;
import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.controller.ArchivoPdf;
import com.example.ejemplodemo.dto.LibroForm;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/admin/libros")
public class AdminLibroController {

    private static final String VISTA_FORMULARIO = "admin/libros/form";
    private static final String REDIRECCION_LISTA = "redirect:/admin/libros";
    private static final String CAMPO_ARCHIVO = "archivo";

    private final LibroAdminService libroAdminService;
    private final LibroService libroService;
    private final CategoriaService categoriaService;

    public AdminLibroController(LibroAdminService libroAdminService,
                                LibroService libroService,
                                CategoriaService categoriaService) {
        this.libroAdminService = libroAdminService;
        this.libroService = libroService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) EstadoLibro estado,
                         @RequestParam(required = false) Long categoria,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        String texto = Formularios.busqueda(q);
        PageRequest solicitud = PageRequest.of(Formularios.pagina(page), Formularios.TAMANO_PAGINA,
                Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        model.addAttribute("pagina", libroAdminService.buscar(texto, estado, categoria, solicitud));
        model.addAttribute("q", texto);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("categoriaFiltro", categoria);
        model.addAttribute("estados", EstadoLibro.values());
        model.addAttribute("categorias", categoriaService.listar());
        return "admin/libros/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new LibroForm());
        return formulario(model, null);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") LibroForm form, BindingResult resultado,
                        @RequestParam(value = CAMPO_ARCHIVO, required = false) MultipartFile archivo,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return formulario(model, null);
        }
        try {
            Libro libro = libroAdminService.crear(form, archivo);
            redirect.addFlashAttribute("exito", "Se registró «" + libro.getTitulo() + "».");
            return REDIRECCION_LISTA;
        } catch (ReglaNegocioException e) {
            mostrarError(resultado, model, e);
        }
        return formulario(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Libro libro = libroAdminService.buscar(id);
        model.addAttribute("form", LibroForm.desde(libro));
        return formulario(model, libro);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") LibroForm form, BindingResult resultado,
                             @RequestParam(value = CAMPO_ARCHIVO, required = false) MultipartFile archivo,
                             Model model, RedirectAttributes redirect) {
        Libro libro = libroAdminService.buscar(id);
        if (resultado.hasErrors()) {
            return formulario(model, libro);
        }
        try {
            libroAdminService.actualizar(id, form, archivo);
            redirect.addFlashAttribute("exito", "Se guardaron los cambios de «" + form.getTitulo() + "».");
            return REDIRECCION_LISTA;
        } catch (ReglaNegocioException e) {
            mostrarError(resultado, model, e);
        }
        return formulario(model, libro);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Libro libro = libroAdminService.eliminar(id);
            redirect.addFlashAttribute("exito", "Se eliminó «" + libro.getTitulo() + "» y su archivo PDF.");
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("error", Formularios.MENSAJE_EN_USO);
        }
        return REDIRECCION_LISTA;
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(@PathVariable Long id, @RequestParam(required = false) String volver,
                          RedirectAttributes redirect) {
        Libro libro = libroService.cambiarEstado(id, EstadoLibro.APROBADO);
        redirect.addFlashAttribute("exito", "«" + libro.getTitulo() + "» fue aprobado y ya está publicado.");
        return destino(volver);
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestParam(required = false) String volver,
                           RedirectAttributes redirect) {
        Libro libro = libroService.cambiarEstado(id, EstadoLibro.RECHAZADO);
        redirect.addFlashAttribute("exito", "«" + libro.getTitulo() + "» fue rechazado y no se muestra en el catálogo.");
        return destino(volver);
    }

    // Permite al administrador revisar el PDF antes de aprobarlo
    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> verArchivo(@PathVariable Long id) {
        Libro libro = libroService.buscar(id);
        return ArchivoPdf.enLinea(libroService.archivoDe(libro), libro.getId());
    }

    // Escritores activos, más el autor actual del libro aunque ya no lo sea (para no perderlo al editar)
    private List<Usuario> escritoresPara(Libro libro) {
        List<Usuario> activos = libroAdminService.escritoresDisponibles();
        Usuario actual = libro == null ? null : libro.getEscritor();
        if (actual == null || activos.stream().anyMatch(escritor -> escritor.getId().equals(actual.getId()))) {
            return activos;
        }
        return Stream.concat(activos.stream(), Stream.of(actual)).toList();
    }

    // Solo se admiten destinos conocidos (evita redirecciones abiertas)
    private static String destino(String volver) {
        return "libros".equals(volver) ? REDIRECCION_LISTA : "redirect:/admin";
    }

    private static void mostrarError(BindingResult resultado, Model model, ReglaNegocioException error) {
        if (CAMPO_ARCHIVO.equals(error.getCampo())) {
            model.addAttribute("errorArchivo", error.getMessage());
        } else {
            Formularios.registrar(resultado, error);
        }
    }

    private String formulario(Model model, Libro libro) {
        model.addAttribute("libro", libro);
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("escritores", escritoresPara(libro));
        model.addAttribute("estados", EstadoLibro.values());
        model.addAttribute("niveles", NivelCuenta.values());
        return VISTA_FORMULARIO;
    }
}
