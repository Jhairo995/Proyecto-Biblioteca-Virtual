package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Service.LectorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final LectorService lectorService;

    public ViewController(LectorService lectorService) {
        this.lectorService = lectorService;
    }

    @GetMapping("/")
    public String inicio() {
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

    @GetMapping("/categorias")
    public String categorias() {
        return "categorias";
    }

    @GetMapping("/suscripcion")
    public String suscripcion() {
        return "suscripcion";
    }
}
