package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.UsuarioService;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario nuevo;

    @BeforeEach
    void preparar() {
        nuevo = new Usuario();
        nuevo.setNombre("Ana");
        nuevo.setCorreo("ana@correo.com");
        nuevo.setDni("12345678");
        nuevo.setContrasena("secreta123");
    }

    @Test
    @DisplayName("Registra un usuario: contraseña cifrada, plan gratuito y rol lector")
    void registraUsuarioNuevo() {
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario guardado = usuarioService.registrarUsuario(nuevo);

        assertEquals("HASH", guardado.getContrasena());
        assertEquals(NivelCuenta.GRATUITO, guardado.getNivelCuenta());
        assertEquals(Rol.LECTOR, guardado.getRol());
        verify(usuarioRepository).save(nuevo);
    }

    @Test
    @DisplayName("Nadie puede registrarse como administrador por la vía pública")
    void rechazaAutoasignarseAdministrador() {
        nuevo.setRol(Rol.ADMINISTRADOR);
        when(usuarioRepository.existsByDni(any())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Rol.LECTOR, usuarioService.registrarUsuario(nuevo).getRol());
    }

    @Test
    @DisplayName("Permite registrarse como escritor")
    void permiteEscritor() {
        nuevo.setRol(Rol.ESCRITOR);
        when(usuarioRepository.existsByDni(any())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Rol.ESCRITOR, usuarioService.registrarUsuario(nuevo).getRol());
    }

    @Test
    @DisplayName("Rechaza un DNI ya registrado y no guarda nada")
    void rechazaDniDuplicado() {
        when(usuarioRepository.existsByDni("12345678")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(nuevo));

        assertTrue(ex.getMessage().contains("DNI"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza un correo ya registrado y no guarda nada")
    void rechazaCorreoDuplicado() {
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(nuevo));

        assertTrue(ex.getMessage().contains("correo"));
        verify(usuarioRepository, never()).save(any());
    }
}
