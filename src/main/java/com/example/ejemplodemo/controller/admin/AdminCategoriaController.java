package com.example.ejemplodemo.controller.admin;

import com.example.ejemplodemo.Service.CategoriaService;
import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.dto.CategoriaForm;
import com.example.ejemplodemo.model.Categoria;
import com.example.ejemplodemo.model.ColorPortada;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categorias")
public class AdminCategoriaController {

    private static final String VISTA_FORMULARIO = "admin/categorias/form";
    private static final String REDIRECCION_LISTA = "redirect:/admin/categorias";

    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("librosPorCategoria", categoriaService.librosPorCategoria());
        return "admin/categorias/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new CategoriaForm());
        return formulario(model, null);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") CategoriaForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return formulario(model, null);
        }
        try {
            Categoria categoria = categoriaService.crear(form);
            redirect.addFlashAttribute("exito", "Se creó la categoría «" + categoria.getNombre() + "».");
            return REDIRECCION_LISTA;
        } catch (ReglaNegocioException e) {
            Formularios.registrar(resultado, e);
        } catch (DataIntegrityViolationException e) {
            resultado.reject("duplicado", Formularios.MENSAJE_DUPLICADO);
        }
        return formulario(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.buscar(id);
        model.addAttribute("form", CategoriaForm.desde(categoria));
        return formulario(model, categoria);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") CategoriaForm form,
                             BindingResult resultado, Model model, RedirectAttributes redirect) {
        Categoria categoria = categoriaService.buscar(id);
        if (resultado.hasErrors()) {
            return formulario(model, categoria);
        }
        try {
            categoriaService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Se guardaron los cambios de «" + form.getNombre() + "».");
            return REDIRECCION_LISTA;
        } catch (ReglaNegocioException e) {
            Formularios.registrar(resultado, e);
        } catch (DataIntegrityViolationException e) {
            resultado.reject("duplicado", Formularios.MENSAJE_DUPLICADO);
        }
        return formulario(model, categoria);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Categoria categoria = categoriaService.eliminar(id);
            redirect.addFlashAttribute("exito", "Se eliminó la categoría «" + categoria.getNombre() + "».");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("error", Formularios.MENSAJE_EN_USO);
        }
        return REDIRECCION_LISTA;
    }

    private String formulario(Model model, Categoria categoria) {
        model.addAttribute("categoria", categoria);
        model.addAttribute("colores", ColorPortada.values());
        return VISTA_FORMULARIO;
    }
}
