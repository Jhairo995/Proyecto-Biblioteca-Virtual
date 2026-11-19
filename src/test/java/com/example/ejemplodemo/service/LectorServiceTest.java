package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.LecturaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.LectorService;
import com.example.ejemplodemo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LectorServiceTest {

    @Mock
    private LibroRepository libroRepository;
    @Mock
    private LecturaRepository lecturaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @InjectMocks
    private LectorService lectorService;

    private Usuario lector;
    private Libro libroGratis;
    private Libro libroPremium;

    @BeforeEach
    void preparar() {
        lector = new Usuario();
        lector.setId(1L);
        lector.setNivelCuenta(NivelCuenta.GRATUITO);

        libroGratis = nuevoLibro(10L, NivelCuenta.GRATUITO, EstadoLibro.APROBADO);
        libroPremium = nuevoLibro(11L, NivelCuenta.PREMIUM, EstadoLibro.APROBADO);
    }

    private Libro nuevoLibro(Long id, NivelCuenta acceso, EstadoLibro estado) {
        Libro libro = new Libro();
        libro.setId(id);
        libro.setTitulo("Libro " + id);
        Categoria categoria = new Categoria();
        categoria.setNombre("Java");
        libro.setCategoria(categoria);
        libro.setNivelAcceso(acceso);
        libro.setEstado(estado);
        return libro;
    }

    @Test
    @DisplayName("Cuenta gratuita: lee libros gratuitos pero no Premium")
    void gratuitoNoLeePremium() {
        assertTrue(lectorService.puedeLeer(lector, libroGratis));
        assertFalse(lectorService.puedeLeer(lector, libroPremium));
    }

    @Test
    @DisplayName("Cuenta Premium: lee todo")
    void premiumLeeTodo() {
        lector.setNivelCuenta(NivelCuenta.PREMIUM);
        assertTrue(lectorService.puedeLeer(lector, libroGratis));
        assertTrue(lectorService.puedeLeer(lector, libroPremium));
    }

    @Test
    @DisplayName("Un libro pendiente no es visible para el lector (404)")
    void libroPendienteNoVisible() {
        Libro pendiente = nuevoLibro(12L, NivelCuenta.GRATUITO, EstadoLibro.PENDIENTE);
        when(libroRepository.findById(12L)).thenReturn(Optional.of(pendiente));

        assertThrows(ResponseStatusException.class, () -> lectorService.libroPublicado(12L));
    }

    @Test
    @DisplayName("Agregar a la lista guarda la lectura si aún no existe")
    void agregaALista() {
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libroGratis));
        when(lecturaRepository.existsByLectorAndLibro(lector, libroGratis)).thenReturn(false);

        lectorService.agregarALista(lector, 10L);

        verify(lecturaRepository).save(any(Lectura.class));
    }

    @Test
    @DisplayName("Agregar un libro que ya está en la lista no duplica el registro")
    void noDuplicaLista() {
        when(libroRepository.findById(10L)).thenReturn(Optional.of(libroGratis));
        when(lecturaRepository.existsByLectorAndLibro(lector, libroGratis)).thenReturn(true);

        lectorService.agregarALista(lector, 10L);

        verify(lecturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Activar Premium cambia el nivel de la cuenta")
    void activaPremium() {
        Usuario enBd = new Usuario();
        enBd.setId(1L);
        enBd.setNivelCuenta(NivelCuenta.GRATUITO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(enBd));

        lectorService.activarPremium(lector);

        assertEquals(NivelCuenta.PREMIUM, enBd.getNivelCuenta());
    }
}
