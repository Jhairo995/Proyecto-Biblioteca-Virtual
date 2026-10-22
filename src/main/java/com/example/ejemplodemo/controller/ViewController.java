package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Service.AutorService;
import com.example.ejemplodemo.Service.LectorService;
import com.example.ejemplodemo.model.Libro;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    private final LectorService lectorService;
    private final AutorService autorService;

    public ViewController(LectorService lectorService, AutorService autorService) {
        this.lectorService = lectorService;
        this.autorService = autorService;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Catálogo público: muestra los libros aprobados por el administrador, opcionalmente de una sola categoría
    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String categoria, Model model) {
        String categoriaActual = categoria == null || categoria.isBlank() ? null : categoria.trim();
        model.addAttribute("libros", lectorService.catalogo(categoriaActual));
        model.addAttribute("categoriaActual", categoriaActual);
        model.addAttribute("categorias", Libro.CATEGORIAS);
        return "catalogo";
    }

    @GetMapping("/categorias")
    public String categorias() {
        return "categorias";
    }

    // Autores: escritores con sus libros publicados y el total de vistas
    @GetMapping("/autores")
    public String autores(Model model) {
        model.addAttribute("autores", autorService.autores());
        return "autores";
    }

    @GetMapping("/suscripcion")
    public String suscripcion() {
        return "suscripcion";
    }
}
