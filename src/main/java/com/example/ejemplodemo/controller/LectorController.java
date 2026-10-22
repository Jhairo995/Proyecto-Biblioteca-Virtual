package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Service.LectorService;
import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lector")
public class LectorController {

    private final LectorService lectorService;
    private final LibroService libroService;

    public LectorController(LectorService lectorService, LibroService libroService) {
        this.lectorService = lectorService;
        this.libroService = libroService;
    }

    @GetMapping
    public String panel(@ModelAttribute(name = "usuarioActual", binding = false) Usuario lector, Model model) {
        model.addAttribute("miLista", lectorService.miLista(lector));
        model.addAttribute("idsEnLista", lectorService.idsEnLista(lector));
        model.addAttribute("catalogo", lectorService.catalogo());
        return "lector";
    }

    @PostMapping("/lista/{id}")
    public String agregar(@ModelAttribute(name = "usuarioActual", binding = false) Usuario lector,
                          @PathVariable Long id, RedirectAttributes redirect) {
        Libro libro = lectorService.agregarALista(lector, id);
        redirect.addFlashAttribute("exito", "«" + libro.getTitulo() + "» se agregó a tu lista.");
        return "redirect:/lector";
    }

    @PostMapping("/lista/{id}/quitar")
    public String quitar(@ModelAttribute(name = "usuarioActual", binding = false) Usuario lector,
                         @PathVariable Long id, RedirectAttributes redirect) {
        Libro libro = lectorService.quitarDeLista(lector, id);
        redirect.addFlashAttribute("exito", "«" + libro.getTitulo() + "» se quitó de tu lista.");
        return "redirect:/lector";
    }

    @GetMapping("/libros/{id}/leer")
    public String leer(@ModelAttribute(name = "usuarioActual", binding = false) Usuario lector,
                       @PathVariable Long id, Model model, RedirectAttributes redirect) {
        Libro libro = lectorService.libroPublicado(id);
        if (!lectorService.puedeLeer(lector, libro)) {
            redirect.addFlashAttribute("error", "«" + libro.getTitulo() + "» es un libro Premium. Mejora tu plan para leerlo.");
            return "redirect:/suscripcion";
        }
        lectorService.registrarVista(libro);
        model.addAttribute("libro", libro);
        return "leer";
    }

    @GetMapping("/libros/{id}/archivo")
    public ResponseEntity<Resource> archivo(@ModelAttribute(name = "usuarioActual", binding = false) Usuario lector,
                                            @PathVariable Long id) {
        Libro libro = lectorService.libroPublicado(id);
        if (!lectorService.puedeLeer(lector, libro)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Libro exclusivo para cuentas Premium.");
        }
        return ArchivoPdf.enLinea(libroService.archivoDe(libro), libro.getId());
    }

    @PostMapping("/premium")
    public String activarPremium(@ModelAttribute(name = "usuarioActual", binding = false) Usuario lector,
                                 RedirectAttributes redirect) {
        lectorService.activarPremium(lector);
        redirect.addFlashAttribute("exito", "¡Bienvenido a Premium! Ya puedes leer toda la biblioteca.");
        return "redirect:/lector";
    }
}
