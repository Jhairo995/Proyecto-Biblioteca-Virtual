package com.example.ejemplodemo.controller.admin;

import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.Service.RolService;
import com.example.ejemplodemo.dto.RolForm;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/roles")
public class AdminRolController {

    private static final String VISTA_FORMULARIO = "admin/roles/form";
    private static final String REDIRECCION_LISTA = "redirect:/admin/roles";

    private final RolService rolService;

    public AdminRolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("roles", rolService.listar());
        model.addAttribute("usuariosPorRol", rolService.usuariosPorRol());
        return "admin/roles/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        RolForm form = new RolForm();
        form.setAcceso(TipoAcceso.LECTOR);
        model.addAttribute("form", form);
        return formulario(model, null);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") RolForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return formulario(model, null);
        }
        try {
            Rol rol = rolService.crear(form);
            redirect.addFlashAttribute("exito", "Se creó el rol «" + rol.getNombre() + "».");
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
        Rol rol = rolService.buscar(id);
        model.addAttribute("form", RolForm.desde(rol));
        return formulario(model, rol);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("form") RolForm form, BindingResult resultado,
                             Model model, RedirectAttributes redirect) {
        Rol rol = rolService.buscar(id);
        if (resultado.hasErrors()) {
            return formulario(model, rol);
        }
        try {
            rolService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Se guardaron los cambios del rol «" + form.getNombre() + "».");
            return REDIRECCION_LISTA;
        } catch (ReglaNegocioException e) {
            Formularios.registrar(resultado, e);
        } catch (DataIntegrityViolationException e) {
            resultado.reject("duplicado", Formularios.MENSAJE_DUPLICADO);
        }
        return formulario(model, rol);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Rol rol = rolService.eliminar(id);
            redirect.addFlashAttribute("exito", "Se eliminó el rol «" + rol.getNombre() + "».");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("error", Formularios.MENSAJE_EN_USO);
        }
        return REDIRECCION_LISTA;
    }

    private String formulario(Model model, Rol rol) {
        model.addAttribute("rol", rol);
        model.addAttribute("accesos", TipoAcceso.values());
        return VISTA_FORMULARIO;
    }
}
