package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.RolService;
import com.example.ejemplodemo.Service.UsuarioService;
import com.example.ejemplodemo.dto.RegistroUsuario;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
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
    @Mock
    private RolService rolService;
    @InjectMocks
    private UsuarioService usuarioService;

    private static RegistroUsuario datos(String rol) {
        return new RegistroUsuario("Ana", "12345678", "ana@correo.com", "secreta123", rol);
    }

    private static Rol rolBase(TipoAcceso acceso) {
        Rol rol = new Rol();
        rol.setNombre(acceso.getEtiqueta());
        rol.setAcceso(acceso);
        rol.setSistema(true);
        return rol;
    }

    private void permitirGuardado() {
        when(usuarioRepository.existsByDni(any())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Registra un usuario: contraseña cifrada, plan gratuito, cuenta activa y rol lector")
    void registraUsuarioNuevo() {
        permitirGuardado();
        when(rolService.rolBase(TipoAcceso.LECTOR)).thenReturn(rolBase(TipoAcceso.LECTOR));

        Usuario guardado = usuarioService.registrarUsuario(datos("LECTOR"));

        assertEquals("HASH", guardado.getContrasena());
        assertEquals(NivelCuenta.GRATUITO, guardado.getNivelCuenta());
        assertEquals(TipoAcceso.LECTOR, guardado.getAcceso());
        assertTrue(guardado.isActivo());
        verify(usuarioRepository).save(guardado);
    }

    @Test
    @DisplayName("Nadie puede registrarse como administrador por la vía pública")
    void rechazaAutoasignarseAdministrador() {
        permitirGuardado();
        when(rolService.rolBase(TipoAcceso.LECTOR)).thenReturn(rolBase(TipoAcceso.LECTOR));

        assertEquals(TipoAcceso.LECTOR, usuarioService.registrarUsuario(datos("ADMINISTRADOR")).getAcceso());
        verify(rolService, never()).rolBase(TipoAcceso.ADMINISTRADOR);
    }

    @Test
    @DisplayName("Permite registrarse como escritor")
    void permiteEscritor() {
        permitirGuardado();
        when(rolService.rolBase(TipoAcceso.ESCRITOR)).thenReturn(rolBase(TipoAcceso.ESCRITOR));

        assertEquals(TipoAcceso.ESCRITOR, usuarioService.registrarUsuario(datos("ESCRITOR")).getAcceso());
    }

    @Test
    @DisplayName("Rechaza un DNI ya registrado y no guarda nada")
    void rechazaDniDuplicado() {
        when(usuarioRepository.existsByDni("12345678")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos("LECTOR")));

        assertTrue(ex.getMessage().contains("DNI"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza un correo ya registrado y no guarda nada")
    void rechazaCorreoDuplicado() {
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario(datos("LECTOR")));

        assertTrue(ex.getMessage().contains("correo"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza contraseñas de más de 72 bytes (BCrypt las truncaría)")
    void rechazaContrasenaDemasiadoLarga() {
        RegistroUsuario largo = new RegistroUsuario("Ana", "12345678", "ana@correo.com", "ñ".repeat(40), "LECTOR");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(largo));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("Aplica la misma regla de nombre que el panel de administración")
    void rechazaNombreConNumeros() {
        RegistroUsuario conNumeros = new RegistroUsuario("Juan2", "12345678", "juan@correo.com", "secreta123", "LECTOR");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(conNumeros));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("Rechaza nombres de más de 100 caracteres")
    void rechazaNombreDemasiadoLargo() {
        RegistroUsuario largo = new RegistroUsuario("A".repeat(101), "12345678", "ana@correo.com", "secreta123", "LECTOR");

        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(largo));
        verifyNoInteractions(usuarioRepository);
    }
}
