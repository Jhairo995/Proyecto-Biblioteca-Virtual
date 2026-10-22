package com.example.ejemplodemo.security;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInicializadorTest {

    private static final String CORREO = "admin@codelibrary.com";
    private static final String DNI = "00000000";

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AdminInicializador inicializador(String password) {
        return new AdminInicializador(usuarioRepository, passwordEncoder, CORREO, DNI, password);
    }

    private static Usuario cuentaConRol(Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setCorreo(CORREO);
        usuario.setRol(rol);
        return usuario;
    }

    @Test
    void creaElAdministradorEnUnaBaseDeDatosVacia() {
        inicializador("Admin#2026").run();

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        Usuario admin = guardado.getValue();
        assertThat(admin.getCorreo()).isEqualTo(CORREO);
        assertThat(admin.getDni()).isEqualTo(DNI);
        assertThat(admin.getRol()).isEqualTo(Rol.ADMINISTRADOR);
        assertThat(admin.getNivelCuenta()).isEqualTo(NivelCuenta.PREMIUM);
        assertThat(passwordEncoder.matches("Admin#2026", admin.getContrasena())).isTrue();
    }

    @Test
    void noDuplicaAlAdministradorAlReiniciar() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(cuentaConRol(Rol.ADMINISTRADOR)));

        inicializador("Admin#2026").run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void sinAdminPasswordNoCreaNada() {
        inicializador("").run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noConvierteEnAdministradorAUnaCuentaQueTomoSuCorreo() {
        Usuario lector = cuentaConRol(Rol.LECTOR);
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(lector));

        inicializador("Admin#2026").run();

        assertThat(lector.getRol()).isEqualTo(Rol.LECTOR);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void siOtraCuentaTieneSuDniAvisaSinTumbarLaAplicacion() {
        when(usuarioRepository.existsByDni(DNI)).thenReturn(true);

        assertThatCode(() -> inicializador("Admin#2026").run()).doesNotThrowAnyException();

        verify(usuarioRepository, never()).save(any());
    }
}
