package com.example.ejemplodemo;

import com.example.ejemplodemo.Repository.CategoriaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.RolRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Prueba de integración del panel de administración: CRUD completos con validación, seguridad real y H2
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin_crud;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "app.admin.password=Admin12345"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCrudTest {

    private static final String CORREO_ADMIN = "admin@codelibrary.com";
    private static final String CLAVE_ADMIN = "Admin12345";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private LibroRepository libroRepository;

    private MockHttpSession admin;

    @BeforeEach
    void iniciarSesion() throws Exception {
        admin = login(CORREO_ADMIN, CLAVE_ADMIN, "/admin");
    }

    @Test
    @DisplayName("Todas las pantallas del panel cargan con el menú lateral")
    void pantallasCargan() throws Exception {
        String[] rutas = {"/admin", "/admin/usuarios", "/admin/usuarios/nuevo", "/admin/roles", "/admin/roles/nuevo",
                "/admin/categorias", "/admin/categorias/nuevo", "/admin/libros", "/admin/libros/nuevo"};
        for (String ruta : rutas) {
            mockMvc.perform(get(ruta).session(admin))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("adminSidebar")));
        }
        Long idAdmin = usuarioRepository.findByCorreo(CORREO_ADMIN).orElseThrow().getId();
        mockMvc.perform(get("/admin/usuarios/{id}", idAdmin).session(admin)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/usuarios/{id}/editar", idAdmin).session(admin)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Los filtros y la paginación toleran valores fuera de rango")
    void filtros() throws Exception {
        mockMvc.perform(get("/admin/usuarios").param("q", "codelibrary").param("estado", "activos").param("page", "-3").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(CORREO_ADMIN)));
        mockMvc.perform(get("/admin/usuarios").param("q", "nadie-con-este-texto").session(admin))
                .andExpect(content().string(containsString("No se encontraron usuarios")));
        mockMvc.perform(get("/admin/libros").param("estado", "PENDIENTE").param("page", "99").session(admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/usuarios").param("rol", "abc").session(admin))
                .andExpect(redirectedUrl("/admin/usuarios"))
                .andExpect(flash().attributeExists("error"));
        mockMvc.perform(get("/admin/usuarios/no-es-un-id").session(admin))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("Categorías: valida, crea, impide duplicados, edita y elimina")
    void crudCategorias() throws Exception {
        long antes = categoriaRepository.count();
        mockMvc.perform(post("/admin/categorias").session(admin)
                        .param("nombre", "   ").param("icono", "<b>malo</b>").param("color", "AZUL"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categorias/form"))
                .andExpect(content().string(containsString("El nombre de la categoría es obligatorio.")))
                .andExpect(content().string(containsString("Usa el nombre de un ícono de Bootstrap Icons")));
        assertEquals(antes, categoriaRepository.count());

        mockMvc.perform(post("/admin/categorias").session(admin)
                        .param("nombre", "Redes").param("icono", "bi-router").param("color", "VERDE")
                        .param("activa", "true").param("_activa", "on"))
                .andExpect(redirectedUrl("/admin/categorias"));
        Long id = categoriaRepository.findByNombreIgnoreCase("Redes").orElseThrow().getId();

        mockMvc.perform(post("/admin/categorias").session(admin)
                        .param("nombre", "REDES").param("icono", "bi-router").param("color", "VERDE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe una categoría")));

        mockMvc.perform(post("/admin/categorias/{id}", id).session(admin)
                        .param("nombre", "Redes y Seguridad").param("icono", "bi-shield-lock").param("color", "OSCURO")
                        .param("_activa", "on"))
                .andExpect(redirectedUrl("/admin/categorias"));
        var editada = categoriaRepository.findById(id).orElseThrow();
        assertEquals("Redes y Seguridad", editada.getNombre());
        assertFalse(editada.isActiva());

        mockMvc.perform(post("/admin/categorias/{id}/eliminar", id).session(admin))
                .andExpect(redirectedUrl("/admin/categorias"))
                .andExpect(flash().attributeExists("exito"));
        assertTrue(categoriaRepository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("Roles: valida, crea, edita y protege los roles del sistema")
    void crudRoles() throws Exception {
        mockMvc.perform(post("/admin/roles").session(admin).param("nombre", "ab").param("acceso", "LECTOR"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("entre 3 y 50 caracteres")));

        mockMvc.perform(post("/admin/roles").session(admin)
                        .param("nombre", "Moderador").param("descripcion", "Revisa libros").param("acceso", "ADMINISTRADOR"))
                .andExpect(redirectedUrl("/admin/roles"));
        Rol moderador = rolRepository.findByNombreIgnoreCase("Moderador").orElseThrow();
        assertEquals(TipoAcceso.ADMINISTRADOR, moderador.getAcceso());

        mockMvc.perform(post("/admin/roles/{id}", moderador.getId()).session(admin)
                        .param("nombre", "Moderador").param("descripcion", "Revisa y aprueba").param("acceso", "ESCRITOR"))
                .andExpect(redirectedUrl("/admin/roles"));
        assertEquals(TipoAcceso.ESCRITOR, rolRepository.findById(moderador.getId()).orElseThrow().getAcceso());

        Rol lector = rolRepository.findFirstByAccesoAndSistemaTrue(TipoAcceso.LECTOR).orElseThrow();
        mockMvc.perform(post("/admin/roles/{id}/eliminar", lector.getId()).session(admin))
                .andExpect(flash().attribute("error", containsString("roles del sistema")));
        mockMvc.perform(post("/admin/roles/{id}", lector.getId()).session(admin)
                        .param("nombre", "Lector").param("acceso", "ADMINISTRADOR"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no se puede cambiar")));

        mockMvc.perform(post("/admin/roles/{id}/eliminar", moderador.getId()).session(admin))
                .andExpect(flash().attributeExists("exito"));
        assertTrue(rolRepository.findById(moderador.getId()).isEmpty());
    }

    @Test
    @DisplayName("Usuarios: valida, crea, visualiza, desactiva (bloquea el login), edita y elimina")
    void crudUsuarios() throws Exception {
        String idLector = String.valueOf(rolRepository.findFirstByAccesoAndSistemaTrue(TipoAcceso.LECTOR).orElseThrow().getId());

        mockMvc.perform(post("/admin/usuarios").session(admin)
                        .param("nombre", "Pedro Ruiz").param("dni", "123").param("correo", "no-es-correo")
                        .param("contrasena", "clave123").param("confirmacion", "clave123").param("rolId", idLector)
                        .param("nivelCuenta", "GRATUITO").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("exactamente 8 dígitos")))
                .andExpect(content().string(containsString("correo electrónico válido")));

        mockMvc.perform(post("/admin/usuarios").session(admin)
                        .param("nombre", "Pedro Ruiz").param("dni", "44445555").param("correo", "pedro@correo.com")
                        .param("contrasena", "clave123").param("confirmacion", "otra").param("rolId", idLector)
                        .param("nivelCuenta", "GRATUITO").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Las contraseñas no coinciden.")));

        mockMvc.perform(post("/admin/usuarios").session(admin)
                        .param("nombre", "Pedro Ruiz").param("dni", "44445555").param("correo", "pedro@correo.com")
                        .param("contrasena", "clave123").param("confirmacion", "clave123").param("rolId", idLector)
                        .param("nivelCuenta", "PREMIUM").param("activo", "true").param("_activo", "on"))
                .andExpect(status().is3xxRedirection());
        Usuario pedro = usuarioRepository.findByCorreo("pedro@correo.com").orElseThrow();
        assertEquals(TipoAcceso.LECTOR, pedro.getAcceso());

        mockMvc.perform(get("/admin/usuarios/{id}", pedro.getId()).session(admin))
                .andExpect(content().string(containsString("44445555")));
        MockHttpSession sesionPedro = login("pedro@correo.com", "clave123", "/lector");

        mockMvc.perform(post("/admin/usuarios/{id}/estado", pedro.getId()).session(admin))
                .andExpect(redirectedUrl("/admin/usuarios"));
        // La sesión que ya tenía abierta deja de valer
        mockMvc.perform(get("/lector").session(sesionPedro)).andExpect(redirectedUrl("/login?expirada"));
        mockMvc.perform(post("/login").param("correo", "pedro@correo.com").param("contrasena", "clave123"))
                .andExpect(redirectedUrl("/login?error"));

        mockMvc.perform(post("/admin/usuarios/{id}", pedro.getId()).session(admin)
                        .param("nombre", "Pedro Ruiz Díaz").param("dni", "44445555").param("correo", "pedro@correo.com")
                        .param("rolId", idLector).param("nivelCuenta", "GRATUITO").param("activo", "true").param("_activo", "on"))
                .andExpect(redirectedUrl("/admin/usuarios/" + pedro.getId()));
        assertEquals("Pedro Ruiz Díaz", usuarioRepository.findById(pedro.getId()).orElseThrow().getNombre());
        login("pedro@correo.com", "clave123", "/lector");

        mockMvc.perform(post("/admin/usuarios/{id}/eliminar", pedro.getId()).session(admin))
                .andExpect(flash().attributeExists("exito"));
        assertTrue(usuarioRepository.findById(pedro.getId()).isEmpty());
    }

    @Test
    @DisplayName("El administrador no puede eliminarse, desactivarse ni quitarse el acceso")
    void administradorProtegido() throws Exception {
        Usuario yo = usuarioRepository.findByCorreo(CORREO_ADMIN).orElseThrow();
        String idLector = String.valueOf(rolRepository.findFirstByAccesoAndSistemaTrue(TipoAcceso.LECTOR).orElseThrow().getId());

        mockMvc.perform(post("/admin/usuarios/{id}/eliminar", yo.getId()).session(admin))
                .andExpect(flash().attribute("error", containsString("tu propia cuenta")));
        mockMvc.perform(post("/admin/usuarios/{id}/estado", yo.getId()).session(admin))
                .andExpect(flash().attribute("error", containsString("tu propia cuenta")));
        mockMvc.perform(post("/admin/usuarios/{id}", yo.getId()).session(admin)
                        .param("nombre", yo.getNombre()).param("dni", yo.getDni()).param("correo", yo.getCorreo())
                        .param("rolId", idLector).param("nivelCuenta", "PREMIUM").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("acceso de administrador a ti mismo")));

        Usuario recargado = usuarioRepository.findById(yo.getId()).orElseThrow();
        assertTrue(recargado.isActivo());
        assertEquals(TipoAcceso.ADMINISTRADOR, recargado.getAcceso());
    }

    @Test
    @DisplayName("Libros: exige PDF, registra, publica, edita, filtra y elimina")
    void crudLibros() throws Exception {
        String idJava = String.valueOf(categoriaRepository.findByNombreIgnoreCase("Java").orElseThrow().getId());
        String titulo = "Libro Admin Integracion";

        mockMvc.perform(multipart("/admin/libros").session(admin)
                        .param("titulo", titulo).param("categoriaId", idJava)
                        .param("nivelAcceso", "GRATUITO").param("estado", "APROBADO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Selecciona el archivo PDF del libro.")));

        MockMultipartFile pdf = new MockMultipartFile("archivo", "libro.pdf", "application/pdf", "%PDF-1.4 admin".getBytes());
        mockMvc.perform(multipart("/admin/libros").file(pdf).session(admin)
                        .param("titulo", titulo).param("categoriaId", idJava).param("idioma", "Español")
                        .param("nivelAcceso", "GRATUITO").param("estado", "APROBADO"))
                .andExpect(redirectedUrl("/admin/libros"));
        Libro libro = libroRepository.findAll().stream().filter(l -> titulo.equals(l.getTitulo())).findFirst().orElseThrow();

        mockMvc.perform(get("/catalogo")).andExpect(content().string(containsString(titulo)));
        mockMvc.perform(get("/admin/libros").param("q", "admin integracion").session(admin))
                .andExpect(content().string(containsString(titulo)));

        mockMvc.perform(post("/admin/categorias/{id}/eliminar", idJava).session(admin))
                .andExpect(flash().attribute("error", containsString("libro(s)")));

        mockMvc.perform(multipart("/admin/libros/{id}", libro.getId()).session(admin)
                        .param("titulo", titulo + " v2").param("categoriaId", idJava)
                        .param("nivelAcceso", "PREMIUM").param("estado", "RECHAZADO"))
                .andExpect(redirectedUrl("/admin/libros"));
        mockMvc.perform(get("/catalogo")).andExpect(content().string(not(containsString(titulo))));

        mockMvc.perform(post("/admin/libros/{id}/aprobar", libro.getId()).param("volver", "libros").session(admin))
                .andExpect(redirectedUrl("/admin/libros"));
        mockMvc.perform(post("/admin/libros/{id}/eliminar", libro.getId()).session(admin))
                .andExpect(redirectedUrl("/admin/libros"));
        assertTrue(libroRepository.findById(libro.getId()).isEmpty());
    }

    @Test
    @DisplayName("Solo el administrador entra al panel")
    void soloAdministrador() throws Exception {
        mockMvc.perform(post("/api/auth/registro").contentType("application/json")
                .content("{\"nombre\":\"Lector Panel\",\"dni\":\"66667777\",\"correo\":\"lectorpanel@correo.com\",\"contrasena\":\"secreta123\",\"rol\":\"LECTOR\"}"))
                .andExpect(status().isCreated());
        MockHttpSession lector = login("lectorpanel@correo.com", "secreta123", "/lector");

        mockMvc.perform(get("/admin/usuarios").session(lector)).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/roles").session(lector).param("nombre", "Intruso").param("acceso", "ADMINISTRADOR"))
                .andExpect(status().isForbidden());
        assertTrue(rolRepository.findByNombreIgnoreCase("Intruso").isEmpty());
    }

    private MockHttpSession login(String correo, String contrasena, String panelEsperado) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/login").param("correo", correo).param("contrasena", contrasena))
                .andExpect(redirectedUrl(panelEsperado))
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }
}
