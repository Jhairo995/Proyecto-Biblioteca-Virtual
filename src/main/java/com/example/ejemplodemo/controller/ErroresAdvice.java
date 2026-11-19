package com.example.ejemplodemo.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Errores que ocurren antes de llegar al controlador (por ejemplo, al leer el PDF subido)
@ControllerAdvice
public class ErroresAdvice {

    // PDF mayor al límite configurado: vuelve al panel del escritor con un mensaje claro
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String archivoDemasiadoGrande(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "El archivo supera el tamaño máximo permitido (20 MB).");
        return "redirect:/escritor";
    }
}
