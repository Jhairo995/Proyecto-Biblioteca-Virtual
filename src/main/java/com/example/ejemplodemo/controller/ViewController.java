package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Service.CategoriaService;
import com.example.ejemplodemo.Service.LectorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final LectorService lectorService;
    private final CategoriaService categoriaService;

    public ViewController(LectorService lectorService, CategoriaService categoriaService) {
        this.lectorService = lectorService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Catálogo público: muestra los libros aprobados por el administrador
    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        model.addAttribute("libros", lectorService.catalogo());
        return "catalogo";
    }

    // Categorías activas administradas desde el panel
    @GetMapping("/categorias")
    public String categorias(Model model) {
        model.addAttribute("categorias", categoriaService.listarActivas());
        return "categorias";
    }

    @GetMapping("/suscripcion")
    public String suscripcion() {
        return "suscripcion";
    }
}
