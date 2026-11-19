package com.example.ejemplodemo.controller.admin;

import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.model.EstadoLibro;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;

// Configuración común a todas las pantallas del panel de administración
@ControllerAdvice(basePackageClasses = AdminAdvice.class)
public class AdminAdvice {

    private final LibroService libroService;

    public AdminAdvice(LibroService libroService) {
        this.libroService = libroService;
    }

    // Contador de libros por revisar que muestra el menú lateral
    @ModelAttribute("pendientesMenu")
    public long pendientesMenu() {
        return libroService.contarPorEstado(EstadoLibro.PENDIENTE);
    }

    // Recorta los textos y convierte los vacíos en null, así "   " cuenta como campo sin completar.
    // Las contraseñas no se recortan: los espacios forman parte de ellas.
    @InitBinder
    public void normalizarTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "contrasena", new VacioComoNulo());
        binder.registerCustomEditor(String.class, "confirmacion", new VacioComoNulo());
    }

    // Un id o filtro con formato inválido (por ejemplo ?rol=abc) vuelve a la pantalla con un aviso en lugar de un error 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String parametroInvalido(MethodArgumentTypeMismatchException error, HttpServletRequest request,
                                    RedirectAttributes redirect) {
        if (error.getParameter().hasParameterAnnotation(PathVariable.class)) {
            redirect.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/admin";
        }
        redirect.addFlashAttribute("error", "Uno de los filtros no es válido; se muestran todos los resultados.");
        return "redirect:" + request.getRequestURI().substring(request.getContextPath().length());
    }

    private static final class VacioComoNulo extends PropertyEditorSupport {
        @Override
        public void setAsText(String texto) {
            setValue(texto == null || texto.isEmpty() ? null : texto);
        }
    }
}
