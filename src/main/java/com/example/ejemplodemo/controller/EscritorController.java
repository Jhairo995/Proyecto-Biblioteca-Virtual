package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.Service.LibroService.DatosLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
@RequestMapping("/escritor")
public class EscritorController {

    private final LibroService libroService;

    public EscritorController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public String panel(@ModelAttribute(name = "usuarioActual", binding = false) Usuario escritor, Model model) {
        model.addAttribute("libros", libroService.librosDeEscritor(escritor));
        model.addAttribute("niveles", NivelCuenta.values());
        return "escritor";
    }

    @PostMapping("/libros")
    public String publicar(@ModelAttribute(name = "usuarioActual", binding = false) Usuario escritor,
                           @RequestParam String titulo,
                           @RequestParam(required = false) String descripcion,
                           @RequestParam String categoria,
                           @RequestParam(required = false) String idioma,
                           @RequestParam(defaultValue = "GRATUITO") NivelCuenta nivelAcceso,
                           @RequestParam("archivo") MultipartFile archivo,
                           RedirectAttributes redirect) {
        try {
            DatosLibro datos = new DatosLibro(titulo, descripcion, categoria, idioma, nivelAcceso);
            Libro libro = libroService.registrarLibro(datos, archivo, escritor);
            redirect.addFlashAttribute("exito",
                    "«" + libro.getTitulo() + "» se envió a revisión. Un administrador debe aprobarlo para publicarlo.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/escritor";
    }

    // El escritor solo puede ver los archivos de sus propios libros
    @GetMapping("/libros/{id}/archivo")
    public ResponseEntity<Resource> verArchivo(@ModelAttribute(name = "usuarioActual", binding = false) Usuario escritor,
                                               @PathVariable Long id) {
        Libro libro = libroService.buscar(id);
        if (libro.getEscritor() == null || !Objects.equals(libro.getEscritor().getId(), escritor.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado.");
        }
        return ArchivoPdf.enLinea(libroService.archivoDe(libro), libro.getId());
    }
}
