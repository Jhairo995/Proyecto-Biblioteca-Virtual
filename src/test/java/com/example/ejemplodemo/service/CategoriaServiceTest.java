package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.CategoriaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Service.CategoriaService;
import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.dto.CategoriaForm;
import com.example.ejemplodemo.model.Categoria;
import com.example.ejemplodemo.model.ColorPortada;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private LibroRepository libroRepository;
    @InjectMocks
    private CategoriaService categoriaService;

    private static CategoriaForm formulario(String nombre, String icono) {
        CategoriaForm form = new CategoriaForm();
        form.setNombre(nombre);
        form.setIcono(icono);
        form.setColor(ColorPortada.AZUL);
        form.setActiva(true);
        return form;
    }

    private static Categoria categoria(Long id, String nombre, boolean activa) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setActiva(activa);
        return categoria;
    }

    @Test
    @DisplayName("Crea una categoría válida")
    void creaCategoria() {
        when(categoriaRepository.existsByNombreIgnoreCase("Redes")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria creada = categoriaService.crear(formulario(" Redes ", "bi-router"));

        assertEquals("Redes", creada.getNombre());
        assertEquals("bi-router", creada.getIcono());
        assertEquals(ColorPortada.AZUL, creada.getColor());
        assertTrue(creada.isActiva());
    }

    @Test
    @DisplayName("Rechaza íconos que no son de Bootstrap Icons")
    void rechazaIconoInvalido() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> categoriaService.crear(formulario("Redes", "<script>")));

        assertEquals("icono", ex.getCampo());
        verifyNoInteractions(categoriaRepository);
    }

    @Test
    @DisplayName("No permite dos categorías con el mismo nombre")
    void rechazaNombreDuplicado() {
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Java", 2L)).thenReturn(true);
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria(2L, "Kotlin", true)));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> categoriaService.actualizar(2L, formulario("Java", "bi-cup")));

        assertEquals("nombre", ex.getCampo());
    }

    @Test
    @DisplayName("No elimina una categoría que tiene libros")
    void noEliminaConLibros() {
        Categoria java = categoria(1L, "Java", true);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(java));
        when(libroRepository.countByCategoria(java)).thenReturn(4L);

        assertThrows(ReglaNegocioException.class, () -> categoriaService.eliminar(1L));
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Una categoría inactiva no se ofrece para libros nuevos, pero se conserva en los que ya la tienen")
    void categoriaInactivaParaLibros() {
        Categoria antigua = categoria(3L, "Antigua", false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(antigua));

        assertThrows(ReglaNegocioException.class, () -> categoriaService.paraLibro(3L, null));
        assertSame(antigua, categoriaService.paraLibro(3L, antigua));
    }
}
