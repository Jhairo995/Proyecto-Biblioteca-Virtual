package com.example.ejemplodemo.controller.admin;

import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.Service.RolService;
import com.example.ejemplodemo.Service.UsuarioAdminService;
import com.example.ejemplodemo.dto.UsuarioForm;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private static final String VISTA_FORMULARIO = "admin/usuarios/form";
    private static final String REDIRECCION_LISTA = "redirect:/admin/usuarios";
    private static final String ESTADO_ACTIVOS = "activos";
    private static final String ESTADO_INACTIVOS = "inactivos";

    private final UsuarioAdminService usuarioAdminService;
    private final RolService rolService;

    public AdminUsuarioController(UsuarioAdminService usuarioAdminService, RolService rolService) {
        this.usuarioAdminService = usuarioAdminService;
        this.rolService = rolService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Long rol,
                         @RequestParam(required = false) String estado,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        String texto = Formularios.busqueda(q);
        Boolean activo = ESTADO_ACTIVOS.equals(estado) ? Boolean.TRUE
                : ESTADO_INACTIVOS.equals(estado) ? Boolean.FALSE : null;
        PageRequest solicitud = PageRequest.of(Formularios.pagina(page), Formularios.TAMANO_PAGINA, Sort.by("nombre"));

        model.addAttribute("pagina", usuarioAdminService.buscar(texto, rol, activo, solicitud));
        model.addAttribute("q", texto);
        model.addAttribute("rolFiltro", rol);
        model.addAttribute("estadoFiltro", activo == null ? null : estado);
        model.addAttribute("roles", rolService.listar());
        return "admin/usuarios/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioAdminService.buscar(id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("libros", usuarioAdminService.librosDe(usuario));
        model.addAttribute("totalLecturas", usuarioAdminService.lecturasDe(usuario));
        return "admin/usuarios/detalle";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new UsuarioForm());
        return formulario(model, null);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return formulario(model, null);
        }
        try {
            Usuario usuario = usuarioAdminService.crear(form);
            redirect.addFlashAttribute("exito", "Se creó la cuenta de «" + usuario.getNombre() + "».");
            return "redirect:/admin/usuarios/" + usuario.getId();
        } catch (ReglaNegocioException e) {
            Formularios.registrar(resultado, e);
        } catch (DataIntegrityViolationException e) {
            resultado.reject("duplicado", Formularios.MENSAJE_DUPLICADO);
        }
        return formulario(model, null);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioAdminService.buscar(id);
        model.addAttribute("form", UsuarioForm.desde(usuario));
        return formulario(model, usuario);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") UsuarioForm form, BindingResult resultado,
                             @ModelAttribute(name = "usuarioActual", binding = false) Usuario actor,
                             Model model, RedirectAttributes redirect) {
        Usuario usuario = usuarioAdminService.buscar(id);
        if (resultado.hasErrors()) {
            return formulario(model, usuario);
        }
        try {
            usuarioAdminService.actualizar(id, form, actor);
            redirect.addFlashAttribute("exito", "Se guardaron los cambios de «" + form.getNombre() + "».");
            return "redirect:/admin/usuarios/" + id;
        } catch (ReglaNegocioException e) {
            Formularios.registrar(resultado, e);
        } catch (DataIntegrityViolationException e) {
            resultado.reject("duplicado", Formularios.MENSAJE_DUPLICADO);
        }
        return formulario(model, usuario);
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam(required = false) String volver,
                                @ModelAttribute(name = "usuarioActual", binding = false) Usuario actor,
                                RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioAdminService.cambiarEstado(id, actor);
            redirect.addFlashAttribute("exito", "La cuenta de «" + usuario.getNombre() + "» ahora está "
                    + (usuario.isActivo() ? "activa." : "desactivada y no podrá iniciar sesión."));
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "detalle".equals(volver) ? "redirect:/admin/usuarios/" + id : REDIRECCION_LISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                           @ModelAttribute(name = "usuarioActual", binding = false) Usuario actor,
                           RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioAdminService.eliminar(id, actor);
            redirect.addFlashAttribute("exito", "Se eliminó la cuenta de «" + usuario.getNombre() + "».");
        } catch (ReglaNegocioException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("error", Formularios.MENSAJE_EN_USO);
        }
        return REDIRECCION_LISTA;
    }

    private String formulario(Model model, Usuario usuario) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.listar());
        model.addAttribute("niveles", NivelCuenta.values());
        return VISTA_FORMULARIO;
    }
}
