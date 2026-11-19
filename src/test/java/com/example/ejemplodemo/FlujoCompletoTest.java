package com.example.ejemplodemo;

import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.model.Libro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba de integración: recorre el flujo completo de la aplicación con H2 y la seguridad real
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flujo_completo;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "app.admin.password=Admin12345"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujoCompletoTest {

    private static final String TITULO = "Libro Flujo Integracion";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibroRepository libroRepository;

    @Test
    @DisplayName("Las páginas públicas cargan sin iniciar sesión")
    void paginasPublicas() throws Exception {
        for (String ruta : new String[]{"/", "/login", "/catalogo", "/categorias", "/suscripcion", "/ejemplo-clase"}) {
            mockMvc.perform(get(ruta)).andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/inicio")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Los paneles exigen iniciar sesión")
    void panelesProtegidos() throws Exception {
        for (String ruta : new String[]{"/admin", "/escritor", "/lector"}) {
            mockMvc.perform(get(ruta)).andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    @DisplayName("El registro rechaza datos incompletos o duplicados con 400")
    void registroInvalido() throws Exception {
        registrar("Dup", "11112222", "dup@correo.com", "LECTOR");

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sin DNI\",\"correo\":\"sindni@correo.com\",\"contrasena\":\"secreta123\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Otro", "11112222", "otro@correo.com", "LECTOR")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Otro", "33334444", "dup@correo.com", "LECTOR")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Reservado", "00000000", "reservado@correo.com", "LECTOR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Escritor publica, administrador aprueba y lector agrega y lee el libro")
    void flujoPublicarAprobarLeer() throws Exception {
        registrar("Escritor Uno", "20000001", "escritor@correo.com", "ESCRITOR");
        registrar("Lector Uno", "20000002", "lector@correo.com", "LECTOR");

        // Escritor: publica un PDF
        MockHttpSession escritor = login("escritor@correo.com", "secreta123", "/escritor");
        mockMvc.perform(get("/escritor").session(escritor)).andExpect(status().isOk());
        MockMultipartFile pdf = new MockMultipartFile("archivo", "libro.pdf", "application/pdf", "%PDF-1.4 prueba".getBytes());
        mockMvc.perform(multipart("/escritor/libros").file(pdf).session(escritor)
                        .param("titulo", TITULO)
                        .param("descripcion", "Descripcion de prueba")
                        .param("categoria", "Java")
                        .param("idioma", "Español")
                        .param("nivelAcceso", "GRATUITO"))
                .andExpect(redirectedUrl("/escritor"));
        mockMvc.perform(get("/escritor").session(escritor))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TITULO)));

        Libro libro = libroRepository.findAll().stream()
                .filter(l -> TITULO.equals(l.getTitulo()))
                .findFirst().orElseThrow();
        mockMvc.perform(get("/escritor/libros/{id}/archivo", libro.getId()).session(escritor))
                .andExpect(status().isOk());

        // Todavía no es público
        mockMvc.perform(get("/catalogo")).andExpect(content().string(not(containsString(TITULO))));

        // Administrador: revisa y aprueba
        MockHttpSession admin = login("admin@codelibrary.com", "Admin12345", "/admin");
        mockMvc.perform(get("/admin").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TITULO)));
        mockMvc.perform(get("/admin/libros/{id}/archivo", libro.getId()).session(admin))
                .andExpect(status().isOk());
        mockMvc.perform(post("/admin/libros/{id}/aprobar", libro.getId()).session(admin))
                .andExpect(redirectedUrl("/admin"));

        // Catálogo público (visitante y escritor)
        mockMvc.perform(get("/catalogo")).andExpect(status().isOk())
                .andExpect(content().string(containsString(TITULO)));
        mockMvc.perform(get("/catalogo").session(escritor)).andExpect(status().isOk())
                .andExpect(content().string(containsString(TITULO)));

        // Lector: agrega a su lista, lee y abre el PDF
        MockHttpSession lector = login("lector@correo.com", "secreta123", "/lector");
        mockMvc.perform(post("/lector/lista/{id}", libro.getId()).session(lector))
                .andExpect(redirectedUrl("/lector"));
        mockMvc.perform(get("/lector").session(lector)).andExpect(status().isOk())
                .andExpect(content().string(containsString(TITULO)));
        mockMvc.perform(get("/lector/libros/{id}/leer", libro.getId()).session(lector))
                .andExpect(status().isOk());
        mockMvc.perform(get("/lector/libros/{id}/archivo", libro.getId()).session(lector))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        mockMvc.perform(post("/lector/lista/{id}/quitar", libro.getId()).session(lector))
                .andExpect(redirectedUrl("/lector"));
        mockMvc.perform(post("/lector/premium").session(lector))
                .andExpect(redirectedUrl("/lector"));

        // Cada rol solo entra a su panel
        mockMvc.perform(get("/admin").session(lector)).andExpect(status().isForbidden());
        mockMvc.perform(get("/escritor").session(lector)).andExpect(status().isForbidden());
        mockMvc.perform(get("/lector").session(escritor)).andExpect(status().isForbidden());

        mockMvc.perform(post("/logout").session(lector)).andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    @DisplayName("Credenciales incorrectas vuelven al login con error")
    void loginInvalido() throws Exception {
        mockMvc.perform(post("/login").param("correo", "nadie@correo.com").param("contrasena", "incorrecta"))
                .andExpect(redirectedUrl("/login?error"));
    }

    private void registrar(String nombre, String dni, String correo, String rol) throws Exception {
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json(nombre, dni, correo, rol)))
                .andExpect(status().isCreated());
    }

    private static String json(String nombre, String dni, String correo, String rol) {
        return "{\"nombre\":\"" + nombre + "\",\"dni\":\"" + dni + "\",\"correo\":\"" + correo
                + "\",\"contrasena\":\"secreta123\",\"rol\":\"" + rol + "\"}";
    }

    private MockHttpSession login(String correo, String contrasena, String panelEsperado) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/login").param("correo", correo).param("contrasena", contrasena))
                .andExpect(redirectedUrl(panelEsperado))
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }
}
