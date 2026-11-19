package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.LecturaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.AlmacenamientoService;
import com.example.ejemplodemo.Service.CategoriaService;
import com.example.ejemplodemo.Service.LibroAdminService;
import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.dto.LibroForm;
import com.example.ejemplodemo.model.Categoria;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroAdminServiceTest {

    private static final Long ID_CATEGORIA = 1L;

    @Mock
    private LibroRepository libroRepository;
    @Mock
    private LecturaRepository lecturaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private AlmacenamientoService almacenamientoService;
    @InjectMocks
    private LibroAdminService libroAdminService;

    private final MockMultipartFile pdf =
            new MockMultipartFile("archivo", "libro.pdf", "application/pdf", "%PDF-1.4".getBytes());

    private static LibroForm formulario() {
        LibroForm form = new LibroForm();
        form.setTitulo("Clean Code");
        form.setCategoriaId(ID_CATEGORIA);
        form.setEstado(EstadoLibro.APROBADO);
        return form;
    }

    private static Usuario usuarioCon(TipoAcceso acceso) {
        Rol rol = new Rol();
        rol.setAcceso(acceso);
        Usuario usuario = new Usuario();
        usuario.setId(9L);
        usuario.setNombre("Autor");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    @Test
    @DisplayName("Registra un libro con su PDF, categoría y estado")
    void creaLibro() {
        Categoria categoria = new Categoria();
        when(categoriaService.paraLibro(ID_CATEGORIA, null)).thenReturn(categoria);
        when(almacenamientoService.guardarPdf(pdf)).thenReturn("nuevo.pdf");
        when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        Libro libro = libroAdminService.crear(formulario(), pdf);

        assertEquals("Clean Code", libro.getTitulo());
        assertEquals("nuevo.pdf", libro.getArchivo());
        assertEquals(EstadoLibro.APROBADO, libro.getEstado());
        assertSame(categoria, libro.getCategoria());
        assertNull(libro.getEscritor());
    }

    @Test
    @DisplayName("Sin PDF no se registra el libro y el error apunta al campo del archivo")
    void crearExigePdf() {
        when(categoriaService.paraLibro(ID_CATEGORIA, null)).thenReturn(new Categoria());
        when(almacenamientoService.guardarPdf(isNull())).thenThrow(new IllegalArgumentException("Selecciona el archivo PDF del libro."));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> libroAdminService.crear(formulario(), null));

        assertEquals("archivo", ex.getCampo());
        verify(libroRepository, never()).save(any());
    }

    @Test
    @DisplayName("El autor debe ser un escritor activo")
    void escritorDebeSerEscritor() {
        LibroForm form = formulario();
        form.setEscritorId(9L);
        when(categoriaService.paraLibro(ID_CATEGORIA, null)).thenReturn(new Categoria());
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(usuarioCon(TipoAcceso.LECTOR)));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> libroAdminService.crear(form, pdf));

        assertEquals("escritorId", ex.getCampo());
        verifyNoInteractions(almacenamientoService);
    }

    @Test
    @DisplayName("Al reemplazar el PDF se borra el archivo anterior")
    void reemplazaPdf() {
        Libro libro = new Libro();
        libro.setId(3L);
        libro.setArchivo("viejo.pdf");
        when(libroRepository.findById(3L)).thenReturn(Optional.of(libro));
        when(categoriaService.paraLibro(ID_CATEGORIA, null)).thenReturn(new Categoria());
        when(almacenamientoService.guardarPdf(pdf)).thenReturn("nuevo.pdf");

        libroAdminService.actualizar(3L, formulario(), pdf);

        assertEquals("nuevo.pdf", libro.getArchivo());
        verify(almacenamientoService).eliminar("viejo.pdf");
    }

    @Test
    @DisplayName("Eliminar un libro quita sus lecturas, el registro y el archivo")
    void eliminaLibro() {
        Libro libro = new Libro();
        libro.setId(3L);
        libro.setArchivo("libro.pdf");
        when(libroRepository.findById(3L)).thenReturn(Optional.of(libro));

        libroAdminService.eliminar(3L);

        verify(lecturaRepository).deleteByLibro(libro);
        verify(libroRepository).delete(libro);
        verify(almacenamientoService).eliminar("libro.pdf");
    }
}
