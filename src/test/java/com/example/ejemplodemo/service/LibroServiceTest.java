package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Service.AlmacenamientoService;
import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.Service.LibroService.DatosLibro;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;
    @Mock
    private AlmacenamientoService almacenamientoService;
    @InjectMocks
    private LibroService libroService;

    private final MockMultipartFile pdf =
            new MockMultipartFile("archivo", "libro.pdf", "application/pdf", "%PDF-1.4".getBytes());

    @Test
    @DisplayName("Todo libro nuevo queda PENDIENTE hasta que un administrador lo revise")
    void libroNuevoQuedaPendiente() {
        when(almacenamientoService.guardarPdf(pdf)).thenReturn("abc.pdf");
        when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        Libro libro = libroService.registrarLibro(
                new DatosLibro("  Clean Code  ", "desc", "Java", "es", null), pdf, new Usuario());

        assertEquals(EstadoLibro.PENDIENTE, libro.getEstado());
        assertEquals("Clean Code", libro.getTitulo());
        assertEquals("abc.pdf", libro.getArchivo());
        assertEquals(NivelCuenta.GRATUITO, libro.getNivelAcceso());
    }

    @Test
    @DisplayName("El título es obligatorio")
    void tituloObligatorio() {
        assertThrows(IllegalArgumentException.class, () -> libroService.registrarLibro(
                new DatosLibro("   ", null, "Java", "es", null), pdf, new Usuario()));
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("La categoría es obligatoria")
    void categoriaObligatoria() {
        assertThrows(IllegalArgumentException.class, () -> libroService.registrarLibro(
                new DatosLibro("Titulo", null, "", "es", null), pdf, new Usuario()));
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("El administrador puede aprobar un libro pendiente")
    void aprobarLibro() {
        Libro libro = new Libro();
        libro.setId(5L);
        when(libroRepository.findById(5L)).thenReturn(Optional.of(libro));

        Libro resultado = libroService.cambiarEstado(5L, EstadoLibro.APROBADO);

        assertEquals(EstadoLibro.APROBADO, resultado.getEstado());
    }
}
