package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final LibroService libroService;
    private final UsuarioRepository usuarioRepository;

    public AdminController(LibroService libroService, UsuarioRepository usuarioRepository) {
        this.libroService = libroService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("pendientes", libroService.librosPorEstado(EstadoLibro.PENDIENTE));
        model.addAttribute("aprobados", libroService.librosPorEstado(EstadoLibro.APROBADO));
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("totalAprobados", libroService.contarPorEstado(EstadoLibro.APROBADO));
        model.addAttribute("totalPendientes", libroService.contarPorEstado(EstadoLibro.PENDIENTE));
        return "admin";
    }

    @PostMapping("/libros/{id}/aprobar")
    public String aprobar(@PathVariable Long id, RedirectAttributes redirect) {
        Libro libro = libroService.cambiarEstado(id, EstadoLibro.APROBADO);
        redirect.addFlashAttribute("exito", "«" + libro.getTitulo() + "» fue aprobado y ya está publicado.");
        return "redirect:/admin";
    }

    @PostMapping("/libros/{id}/rechazar")
    public String rechazar(@PathVariable Long id, RedirectAttributes redirect) {
        Libro libro = libroService.cambiarEstado(id, EstadoLibro.RECHAZADO);
        redirect.addFlashAttribute("exito", "«" + libro.getTitulo() + "» fue rechazado.");
        return "redirect:/admin";
    }

    // Permite al administrador revisar el PDF antes de aprobarlo
    @GetMapping("/libros/{id}/archivo")
    public ResponseEntity<Resource> verArchivo(@PathVariable Long id) {
        Libro libro = libroService.buscar(id);
        return ArchivoPdf.enLinea(libroService.archivoDe(libro), libro.getId());
    }
}
