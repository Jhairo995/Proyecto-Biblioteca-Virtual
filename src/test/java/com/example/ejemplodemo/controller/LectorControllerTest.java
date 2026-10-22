package com.example.ejemplodemo.controller;

import com.example.ejemplodemo.Service.LectorService;
import com.example.ejemplodemo.Service.LibroService;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LectorControllerTest {

    private static final Long ID_LIBRO = 1L;

    @Mock
    private LectorService lectorService;
    @Mock
    private LibroService libroService;

    private LectorController controller;
    private final Usuario lector = new Usuario();
    private final Libro libro = new Libro();

    @BeforeEach
    void setUp() {
        controller = new LectorController(lectorService, libroService);
        libro.setTitulo("Clean Code");
        when(lectorService.libroPublicado(ID_LIBRO)).thenReturn(libro);
    }

    @Test
    void abrirUnLibroEnElVisorRegistraUnaVista() {
        when(lectorService.puedeLeer(lector, libro)).thenReturn(true);

        String vista = controller.leer(lector, ID_LIBRO, new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertThat(vista).isEqualTo("leer");
        verify(lectorService).registrarVista(libro);
    }

    @Test
    void unLibroPremiumBloqueadoNoSumaVistas() {
        when(lectorService.puedeLeer(lector, libro)).thenReturn(false);

        String vista = controller.leer(lector, ID_LIBRO, new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertThat(vista).isEqualTo("redirect:/suscripcion");
        verify(lectorService, never()).registrarVista(any());
    }
}
