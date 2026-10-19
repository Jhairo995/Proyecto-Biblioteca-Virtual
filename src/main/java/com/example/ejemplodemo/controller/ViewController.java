package com.example.ejemplodemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/catalogo")
    public String catalogo() {
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