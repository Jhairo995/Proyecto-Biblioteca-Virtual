package com.example.ejemplodemo.controller;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

// Las vistas entregan los mensajes como elementos data-aviso que app.js convierte en avisos (toasts)
@SpringBootTest
@AutoConfigureMockMvc
class VistasTest {

    private static final String AVISO_ERROR = "data-aviso=\"danger\"";
    private static final String AVISO_EXITO = "data-aviso=\"success\"";

    @Autowired
    private MockMvcTester mvc;

    @Test
    void loginFallidoMuestraAvisoDeError() {
        assertThat(mvc.get().uri("/login?error"))
                .hasStatusOk()
                .bodyText()
                .contains(AVISO_ERROR)
                .contains("Correo o contraseña incorrectos.");
    }

    @Test
    void cerrarSesionMuestraAvisoDeExito() {
        assertThat(mvc.get().uri("/login?logout"))
                .hasStatusOk()
                .bodyText()
                .contains(AVISO_EXITO)
                .doesNotContain(AVISO_ERROR);
    }

    @Test
    void loginSinParametrosNoMuestraAvisos() {
        assertThat(mvc.get().uri("/login"))
                .hasStatusOk()
                .bodyText()
                .doesNotContain("data-aviso=");
    }

    @Test
    void losBotonesParaCrearCuentaAbrenElRegistro() {
        assertThat(mvc.get().uri("/"))
                .hasStatusOk()
                .bodyText()
                .contains("href=\"/login#registro\"");
    }

    @Test
    void verRecursosDeUnaCategoriaEnlazaAlCatalogoFiltrado() {
        assertThat(mvc.get().uri("/categorias"))
                .hasStatusOk()
                .bodyText()
                .contains("href=\"/catalogo?categoria=Java\"")
                .contains("href=\"/catalogo?categoria=Desarrollo%20Web\"");
    }

    @Test
    void catalogoFiltradoIndicaLaCategoriaElegida() {
        assertThat(mvc.get().uri("/catalogo").param("categoria", "Java"))
                .hasStatusOk()
                .bodyText()
                .contains("Libros de <span class=\"text-gradient\">Java</span>")
                .contains("aria-current=\"page\"");
    }

    @Test
    void categoriaSinLibrosMuestraAvisoEnVezDeLibrosDeMuestra() {
        assertThat(mvc.get().uri("/catalogo").param("categoria", "Categoría inexistente"))
                .hasStatusOk()
                .bodyText()
                .contains("Aún no hay libros publicados en «Categoría inexistente»")
                .doesNotContain("JavaScript Moderno");
    }

    @Test
    void accesoDenegadoUsaLaPaginaDeErrorPropia() {
        assertThat(mvc.get().uri("/error").accept(MediaType.TEXT_HTML)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .hasStatus(HttpStatus.FORBIDDEN)
                .bodyText()
                .contains("No tienes acceso a esta página");
    }

    @Test
    void paginaInexistenteUsaLaPaginaDeErrorPropia() {
        assertThat(mvc.get().uri("/error").accept(MediaType.TEXT_HTML)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyText()
                .contains("No encontramos esta página");
    }

    @Test
    void laPaginaDeErrorNoConvierteElMotivoEnAviso() {
        // BasicErrorController pone "error" (p. ej. "Not Found") en el modelo, igual que los flash de los controladores
        assertThat(mvc.get().uri("/error").accept(MediaType.TEXT_HTML)
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .bodyText()
                .doesNotContain("data-aviso=");
    }
}
