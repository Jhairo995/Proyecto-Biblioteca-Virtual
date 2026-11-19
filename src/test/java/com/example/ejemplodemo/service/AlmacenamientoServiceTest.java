package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Service.AlmacenamientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AlmacenamientoServiceTest {

    @TempDir
    Path temporal;

    private AlmacenamientoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AlmacenamientoService(temporal.toString());
    }

    @Test
    @DisplayName("Guarda un PDF válido con nombre aleatorio y luego lo puede cargar")
    void guardaYCargaPdf() {
        MockMultipartFile pdf = new MockMultipartFile("archivo", "mi libro.pdf", "application/pdf", "%PDF-1.7 contenido".getBytes());

        String nombre = servicio.guardarPdf(pdf);

        assertTrue(nombre.endsWith(".pdf"));
        assertNotEquals("mi libro.pdf", nombre);
        assertTrue(servicio.cargar(nombre).exists());
    }

    @Test
    @DisplayName("Rechaza archivos que no tienen extensión .pdf")
    void rechazaExtensionIncorrecta() {
        MockMultipartFile exe = new MockMultipartFile("archivo", "malicioso.exe", "application/octet-stream", "%PDF".getBytes());
        assertThrows(IllegalArgumentException.class, () -> servicio.guardarPdf(exe));
    }

    @Test
    @DisplayName("Rechaza un archivo .pdf cuyo contenido real no es PDF")
    void rechazaFirmaFalsa() {
        MockMultipartFile falso = new MockMultipartFile("archivo", "falso.pdf", "application/pdf", "no soy pdf".getBytes());
        assertThrows(IllegalArgumentException.class, () -> servicio.guardarPdf(falso));
    }

    @Test
    @DisplayName("Rechaza un archivo vacío")
    void rechazaVacio() {
        MockMultipartFile vacio = new MockMultipartFile("archivo", "vacio.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> servicio.guardarPdf(vacio));
    }

    @Test
    @DisplayName("Bloquea el intento de salir del directorio (path traversal)")
    void bloqueaPathTraversal() {
        assertThrows(ResponseStatusException.class, () -> servicio.cargar("../../etc/passwd"));
    }
}
