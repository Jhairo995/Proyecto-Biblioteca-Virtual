package com.example.ejemplodemo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Errores que ocurren antes de llegar al controlador (por ejemplo, al leer el PDF subido)
@ControllerAdvice
public class ErroresAdvice {

    // PDF mayor al límite configurado: vuelve al panel desde el que se envió con un mensaje claro
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String archivoDemasiadoGrande(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "El archivo supera el tamaño máximo permitido (20 MB).");
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        return ruta.startsWith("/admin") ? "redirect:/admin/libros" : "redirect:/escritor";
    }
}
