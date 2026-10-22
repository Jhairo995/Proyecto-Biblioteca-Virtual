package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.UsuarioService.DatosRegistro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String CORREO_ADMIN = "admin@codelibrary.com";
    private static final String DNI_ADMIN = "00000000";

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, CORREO_ADMIN, DNI_ADMIN);
    }

    private static DatosRegistro datos(String dni, String correo, Rol rol) {
        return new DatosRegistro("Ana Lectora", dni, correo, "secreto1", rol);
    }

    private void guardarDevuelveElUsuario() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void registraLectorConContrasenaCifradaYPlanGratuito() {
        guardarDevuelveElUsuario();

        Usuario usuario = usuarioService.registrarUsuario(
                new DatosRegistro("  Ana Lectora ", " 10000001 ", " ana@test.com ", "secreto1", Rol.LECTOR));

        assertThat(usuario.getNombre()).isEqualTo("Ana Lectora");
        assertThat(usuario.getDni()).isEqualTo("10000001");
        assertThat(usuario.getCorreo()).isEqualTo("ana@test.com");
        assertThat(usuario.getRol()).isEqualTo(Rol.LECTOR);
        assertThat(usuario.getNivelCuenta()).isEqualTo(NivelCuenta.GRATUITO);
        assertThat(passwordEncoder.matches("secreto1", usuario.getContrasena())).isTrue();
    }

    @Test
    void registraEscritorCuandoLoElige() {
        guardarDevuelveElUsuario();

        Usuario usuario = usuarioService.registrarUsuario(datos("10000002", "beto@test.com", Rol.ESCRITOR));

        assertThat(usuario.getRol()).isEqualTo(Rol.ESCRITOR);
    }

    @Test
    void quienPideSerAdministradorQuedaComoLector() {
        guardarDevuelveElUsuario();

        Usuario usuario = usuarioService.registrarUsuario(datos("10000003", "hack@test.com", Rol.ADMINISTRADOR));

        assertThat(usuario.getRol()).isEqualTo(Rol.LECTOR);
        assertThat(usuario.getId()).isNull();
    }

    @Test
    void sinRolQuedaComoLector() {
        guardarDevuelveElUsuario();

        Usuario usuario = usuarioService.registrarUsuario(datos("10000004", "caro@test.com", null));

        assertThat(usuario.getRol()).isEqualTo(Rol.LECTOR);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "NULL,         10000001,  ana@test.com, secreto1, El nombre es obligatorio",
            "'   ',        10000001,  ana@test.com, secreto1, El nombre es obligatorio",
            "Ana,          NULL,      ana@test.com, secreto1, El DNI debe tener",
            "Ana,          1234567,   ana@test.com, secreto1, El DNI debe tener",
            "Ana,          123456789, ana@test.com, secreto1, El DNI debe tener",
            "Ana,          abcdefgh,  ana@test.com, secreto1, El DNI debe tener",
            "Ana,          10000001,  NULL,         secreto1, El correo electrónico no es válido",
            "Ana,          10000001,  noesuncorreo, secreto1, El correo electrónico no es válido",
            "Ana,          10000001,  ana@test.com, NULL,     La contraseña debe tener",
            "Ana,          10000001,  ana@test.com, '',       La contraseña debe tener",
            "Ana,          10000001,  ana@test.com, 12345,    La contraseña debe tener",
    })
    void rechazaDatosInvalidosSinGuardar(String nombre, String dni, String correo, String contrasena, String mensaje) {
        DatosRegistro invalidos = new DatosRegistro(nombre, dni, correo, contrasena, Rol.LECTOR);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(invalidos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith(mensaje);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaContrasenaQueBcryptNoPuedeCifrar() {
        DatosRegistro larga = new DatosRegistro("Ana", "10000001", "ana@test.com", "x".repeat(73), Rol.LECTOR);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(larga))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("72");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void reservaElCorreoDelAdministradorAunqueNoExistaTodavia() {
        assertThatThrownBy(() -> usuarioService.registrarUsuario(datos("10000005", "ADMIN@codelibrary.com", Rol.LECTOR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reservado");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void reservaElDniDelAdministradorAunqueNoExistaTodavia() {
        assertThatThrownBy(() -> usuarioService.registrarUsuario(datos(DNI_ADMIN, "zeta@test.com", Rol.LECTOR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reservado");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaDniDuplicado() {
        when(usuarioRepository.existsByDni("10000001")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(datos("10000001", "otra@test.com", Rol.LECTOR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El DNI ya está registrado en el sistema.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarioRepository.existsByCorreo("ana@test.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(datos("10000006", "ana@test.com", Rol.LECTOR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El correo ya está registrado en el sistema.");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registroSimultaneoConMismoCorreoNoTerminaEnError500() {
        when(usuarioRepository.save(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> usuarioService.registrarUsuario(datos("10000007", "ana@test.com", Rol.LECTOR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");
    }
}
