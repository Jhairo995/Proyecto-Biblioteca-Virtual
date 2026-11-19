package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.RolRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.Service.RolService;
import com.example.ejemplodemo.dto.RolForm;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.security.SesionesUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private SesionesUsuario sesionesUsuario;
    @InjectMocks
    private RolService rolService;

    private static RolForm formulario(String nombre, TipoAcceso acceso) {
        RolForm form = new RolForm();
        form.setNombre(nombre);
        form.setDescripcion("  Descripción  ");
        form.setAcceso(acceso);
        return form;
    }

    private static Rol rol(Long id, String nombre, TipoAcceso acceso, boolean sistema) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        rol.setAcceso(acceso);
        rol.setSistema(sistema);
        return rol;
    }

    @Test
    @DisplayName("Crea un rol personalizado con los textos normalizados")
    void creaRol() {
        when(rolRepository.existsByNombreIgnoreCase("Moderador")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));

        Rol creado = rolService.crear(formulario("  Moderador ", TipoAcceso.ADMINISTRADOR));

        assertEquals("Moderador", creado.getNombre());
        assertEquals("Descripción", creado.getDescripcion());
        assertEquals(TipoAcceso.ADMINISTRADOR, creado.getAcceso());
        assertFalse(creado.isSistema());
    }

    @Test
    @DisplayName("No permite dos roles con el mismo nombre (sin distinguir mayúsculas)")
    void rechazaNombreDuplicado() {
        when(rolRepository.existsByNombreIgnoreCase("lector")).thenReturn(true);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> rolService.crear(formulario("lector", TipoAcceso.LECTOR)));

        assertEquals("nombre", ex.getCampo());
        verify(rolRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un rol del sistema no puede cambiar su nivel de acceso")
    void rolSistemaConservaAcceso() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol(1L, "Lector", TipoAcceso.LECTOR, true)));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> rolService.actualizar(1L, formulario("Lector", TipoAcceso.ADMINISTRADOR)));

        assertEquals("acceso", ex.getCampo());
    }

    @Test
    @DisplayName("Un rol del sistema no puede cambiar su nombre")
    void rolSistemaConservaNombre() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol(1L, "Lector", TipoAcceso.LECTOR, true)));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> rolService.actualizar(1L, formulario("Visitante", TipoAcceso.LECTOR)));

        assertEquals("nombre", ex.getCampo());
    }

    @Test
    @DisplayName("No permite quitar el acceso de administrador si deja la plataforma sin administradores")
    void noDejaSinAdministradores() {
        Rol moderador = rol(7L, "Moderador", TipoAcceso.ADMINISTRADOR, false);
        when(rolRepository.findById(7L)).thenReturn(Optional.of(moderador));
        when(rolRepository.existsByNombreIgnoreCaseAndIdNot("Moderador", 7L)).thenReturn(false);
        when(usuarioRepository.contarActivosPorAccesoFueraDeRol(TipoAcceso.ADMINISTRADOR, moderador)).thenReturn(0L);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> rolService.actualizar(7L, formulario("Moderador", TipoAcceso.LECTOR)));

        assertEquals("acceso", ex.getCampo());
        assertEquals(TipoAcceso.ADMINISTRADOR, moderador.getAcceso());
    }

    @Test
    @DisplayName("Cambiar el nivel de acceso de un rol cierra las sesiones de sus usuarios")
    void cambiarAccesoCierraSesiones() {
        Rol moderador = rol(7L, "Moderador", TipoAcceso.ESCRITOR, false);
        Usuario miembro = new Usuario();
        miembro.setCorreo("miembro@correo.com");
        when(rolRepository.findById(7L)).thenReturn(Optional.of(moderador));
        when(rolRepository.existsByNombreIgnoreCaseAndIdNot("Moderador", 7L)).thenReturn(false);
        when(usuarioRepository.findByRol(moderador)).thenReturn(List.of(miembro));

        rolService.actualizar(7L, formulario("Moderador", TipoAcceso.LECTOR));

        assertEquals(TipoAcceso.LECTOR, moderador.getAcceso());
        verify(sesionesUsuario).cerrar("miembro@correo.com");
    }

    @Test
    @DisplayName("Los roles del sistema no se pueden eliminar")
    void noEliminaRolSistema() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol(1L, "Lector", TipoAcceso.LECTOR, true)));

        assertThrows(ReglaNegocioException.class, () -> rolService.eliminar(1L));
        verify(rolRepository, never()).delete(any());
    }

    @Test
    @DisplayName("No elimina un rol que todavía tiene usuarios")
    void noEliminaRolConUsuarios() {
        Rol moderador = rol(7L, "Moderador", TipoAcceso.ADMINISTRADOR, false);
        when(rolRepository.findById(7L)).thenReturn(Optional.of(moderador));
        when(usuarioRepository.countByRol(moderador)).thenReturn(3L);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> rolService.eliminar(7L));

        assertTrue(ex.getMessage().contains("3 usuario(s)"));
        verify(rolRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Elimina un rol personalizado sin usuarios")
    void eliminaRolLibre() {
        Rol moderador = rol(7L, "Moderador", TipoAcceso.ADMINISTRADOR, false);
        when(rolRepository.findById(7L)).thenReturn(Optional.of(moderador));
        when(usuarioRepository.countByRol(moderador)).thenReturn(0L);

        rolService.eliminar(7L);

        verify(rolRepository).delete(moderador);
    }
}
