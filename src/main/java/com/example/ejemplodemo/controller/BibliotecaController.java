package com.example.ejemplodemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EjemploController {

    // 1. CONSTANTES EN JAVA
    public static final String NOMBRE_CURSO = "Desarrollo Web Integrado";
    public static final double TASA_IGV = 0.18;
    public static final int EDAD_MINIMA_LEGAL = 18;

    @GetMapping("/")
    public String mostrarEnPantalla(
            @RequestParam(defaultValue = "Carlos Silva") String nombreEstudiante,
            @RequestParam(defaultValue = "20") int edadEstudiante,
            @RequestParam(defaultValue = "16.5") double notaFinal,
            @RequestParam(defaultValue = "VIP") String tipoMembresia,
            Model model) {
        // 1. VARIABLES

        // Enteros
        byte nivelAcceso = 3;
        short anioIngreso = 2024;
        int edad = edadEstudiante;
        long idEstudiante = 10002938475L;

        // Decimales
        float pesoKg = 72.5f;
        double nota = notaFinal;

        // Carácter y Booleano
        char seccion = 'A';
        boolean estaMatriculado = true;

        // Cadena de texto (Objeto)
        String nombre = nombreEstudiante;

        // 2. ESTRUCTURAS DE CONTROL

        // A) Validación
        String validacionEdad;
        if (edad < 0 || edad > 120) {
            validacionEdad = "Edad no valida";
        } else if (edad < EDAD_MINIMA_LEGAL) {
            validacionEdad = "Menor de edad (Requiere tutor)";
        } else if (edad <= 65) {
            validacionEdad = "Mayor de edad (Adulto)";
        } else {
            validacionEdad = "Adulto Mayor";
        }

        // B) Validación
        String estadoAcademico;
        if (nota >= 14.0) {
            estadoAcademico = "Aprobado (Excelente)";
        } else if (nota >= 11.0) {
            estadoAcademico = "Aprobado (Regular)";
        } else {
            estadoAcademico = "Desaprobado";
        }

        // C) Validación
        String beneficioMembresia;
        switch (tipoMembresia.toUpperCase()) {
            case "VIP":
                beneficioMembresia = "Acceso Total y Tutorias";
                break;
            case "PREMIUM":
                beneficioMembresia = "Acceso a modulos completos";
                break;
            case "BASICO":
                beneficioMembresia = "Acceso estandar";
                break;
            default:
                beneficioMembresia = "Acceso Invitado";
                break;
        }

        // D) Operador Ternario
        String estadoMatricula = estaMatriculado ? "Activo" : "Inactivo";

        // 3. ENVIAR DATOS A LA VISTA
        model.addAttribute("nombreCurso", NOMBRE_CURSO);
        model.addAttribute("tasaIgv", TASA_IGV);
        model.addAttribute("nivelAcceso", nivelAcceso);
        model.addAttribute("anioIngreso", anioIngreso);
        model.addAttribute("edad", edad);
        model.addAttribute("idEstudiante", idEstudiante);
        model.addAttribute("pesoKg", pesoKg);
        model.addAttribute("nota", nota);
        model.addAttribute("seccion", seccion);
        model.addAttribute("estaMatriculado", estaMatriculado);
        model.addAttribute("nombre", nombre);
        model.addAttribute("tipoMembresia", tipoMembresia);

        model.addAttribute("validacionEdad", validacionEdad);
        model.addAttribute("estadoAcademico", estadoAcademico);
        model.addAttribute("beneficioMembresia", beneficioMembresia);
        model.addAttribute("estadoMatricula", estadoMatricula);

        return "ejemplo";
    }
}
