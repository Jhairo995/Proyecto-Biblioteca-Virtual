package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.LecturaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LectorServiceTest {

    @Mock
    private LibroRepository libroRepository;
    @Mock
    private LecturaRepository lecturaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private LectorService lectorService;

    @BeforeEach
    void setUp() {
        lectorService = new LectorService(libroRepository, lecturaRepository, usuarioRepository);
    }

    private static Libro libroDe(String categoria) {
        Libro libro = new Libro();
        libro.setCategoria(categoria);
        libro.setEstado(EstadoLibro.APROBADO);
        return libro;
    }

    @Test
    void catalogoConCategoriaSoloDevuelveLosAprobadosDeEsaCategoria() {
        List<Libro> deJava = List.of(libroDe("Java"));
        when(libroRepository.findByEstadoAndCategoriaIgnoreCaseOrderByFechaRegistroDesc(EstadoLibro.APROBADO, "Java"))
                .thenReturn(deJava);

        assertThat(lectorService.catalogo("Java")).isEqualTo(deJava);
        verify(libroRepository, never()).findByEstadoOrderByFechaRegistroDesc(any());
    }

    @Test
    void catalogoIgnoraLosEspaciosAlrededorDeLaCategoria() {
        lectorService.catalogo("  Bases de Datos ");

        verify(libroRepository).findByEstadoAndCategoriaIgnoreCaseOrderByFechaRegistroDesc(EstadoLibro.APROBADO, "Bases de Datos");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void catalogoSinCategoriaDevuelveTodosLosAprobados(String categoria) {
        List<Libro> todos = List.of(libroDe("Java"), libroDe("Otros"));
        when(libroRepository.findByEstadoOrderByFechaRegistroDesc(EstadoLibro.APROBADO)).thenReturn(todos);

        assertThat(lectorService.catalogo(categoria)).isEqualTo(todos);
        verify(libroRepository, never()).findByEstadoAndCategoriaIgnoreCaseOrderByFechaRegistroDesc(any(), anyString());
    }
}
