package com.example.ejemplodemo.controller.admin;

import com.example.ejemplodemo.Service.CategoriaService;
import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.Service.RolService;
import com.example.ejemplodemo.Service.UsuarioAdminService;
import com.example.ejemplodemo.model.EstadoLibro;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Resumen general del panel de administración
@Controller
@RequestMapping("/admin")
public class AdminPanelController {

    private final LibroService libroService;
    private final UsuarioAdminService usuarioAdminService;
    private final CategoriaService categoriaService;
    private final RolService rolService;

    public AdminPanelController(LibroService libroService,
                                UsuarioAdminService usuarioAdminService,
                                CategoriaService categoriaService,
                                RolService rolService) {
        this.libroService = libroService;
        this.usuarioAdminService = usuarioAdminService;
        this.categoriaService = categoriaService;
        this.rolService = rolService;
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("pendientes", libroService.ultimosPorEstado(EstadoLibro.PENDIENTE));
        model.addAttribute("totalUsuarios", usuarioAdminService.contarTodos());
        model.addAttribute("usuariosActivos", usuarioAdminService.contarActivos());
        model.addAttribute("totalLibros", libroService.contarTodos());
        model.addAttribute("totalAprobados", libroService.contarPorEstado(EstadoLibro.APROBADO));
        model.addAttribute("totalPendientes", libroService.contarPorEstado(EstadoLibro.PENDIENTE));
        model.addAttribute("totalRechazados", libroService.contarPorEstado(EstadoLibro.RECHAZADO));
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("librosPorCategoria", categoriaService.librosPorCategoria());
        model.addAttribute("roles", rolService.listar());
        model.addAttribute("usuariosPorRol", rolService.usuariosPorRol());
        return "admin/panel";
    }
}
