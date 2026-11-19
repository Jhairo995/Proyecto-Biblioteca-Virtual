package com.example.ejemplodemo.service;

import com.example.ejemplodemo.Repository.LecturaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.ReglaNegocioException;
import com.example.ejemplodemo.Service.RolService;
import com.example.ejemplodemo.Service.UsuarioAdminService;
import com.example.ejemplodemo.dto.UsuarioForm;
import com.example.ejemplodemo.model.NivelCuenta;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioAdminServiceTest {

    private static final Long ID_ROL_LECTOR = 1L;
    private static final Long ID_ROL_ADMIN = 3L;

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private LibroRepository libroRepository;
    @Mock
    private LecturaRepository lecturaRepository;
    @Mock
    private RolService rolService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SesionesUsuario sesionesUsuario;
    @InjectMocks
    private UsuarioAdminService usuarioAdminService;

    private static Rol rol(Long id, TipoAcceso acceso) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(acceso.getEtiqueta());
        rol.setAcceso(acceso);
        return rol;
    }

    private static Usuario usuario(Long id, TipoAcceso acceso) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Usuario de Prueba");
        usuario.setCorreo("usuario" + id + "@correo.com");
        usuario.setDni(String.format("%08d", 10000000 + id));
        usuario.setContrasena("HASH-ANTERIOR");
        usuario.setRol(rol(acceso == TipoAcceso.ADMINISTRADOR ? ID_ROL_ADMIN : ID_ROL_LECTOR, acceso));
        usuario.setActivo(true);
        return usuario;
    }

    private static UsuarioForm formulario(Usuario base, Long rolId) {
        UsuarioForm form = UsuarioForm.desde(base);
        form.setRolId(rolId);
        return form;
    }

    private static UsuarioForm formularioNuevo() {
        UsuarioForm form = new UsuarioForm();
        form.setNombre("Ana Torres");
        form.setDni("12345678");
        form.setCorreo("ana@correo.com");
        form.setContrasena("secreta123");
        form.setConfirmacion("secreta123");
        form.setRolId(ID_ROL_LECTOR);
        form.setNivelCuenta(NivelCuenta.PREMIUM);
        return form;
    }

    @Test
    @DisplayName("Crea un usuario con contraseña cifrada, rol y plan elegidos")
    void creaUsuario() {
        when(usuarioRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@correo.com")).thenReturn(false);
        when(rolService.paraAsignar(ID_ROL_LECTOR)).thenReturn(rol(ID_ROL_LECTOR, TipoAcceso.LECTOR));
        when(passwordEncoder.encode("secreta123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario creado = usuarioAdminService.crear(formularioNuevo());

        assertEquals("HASH", creado.getContrasena());
        assertEquals(NivelCuenta.PREMIUM, creado.getNivelCuenta());
        assertEquals(TipoAcceso.LECTOR, creado.getAcceso());
    }

    @Test
    @DisplayName("Al crear, la contraseña es obligatoria")
    void crearExigeContrasena() {
        UsuarioForm form = formularioNuevo();
        form.setContrasena(null);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> usuarioAdminService.crear(form));

        assertEquals("contrasena", ex.getCampo());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("La confirmación debe coincidir con la contraseña")
    void confirmacionDistinta() {
        UsuarioForm form = formularioNuevo();
        form.setConfirmacion("otra-clave");

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> usuarioAdminService.crear(form));

        assertEquals("confirmacion", ex.getCampo());
    }

    @Test
    @DisplayName("El DNI reservado del administrador inicial no se puede usar")
    void dniReservado() {
        UsuarioForm form = formularioNuevo();
        form.setDni("00000000");

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> usuarioAdminService.crear(form));

        assertEquals("dni", ex.getCampo());
    }

    @Test
    @DisplayName("Editar sin escribir contraseña conserva la actual")
    void editarConservaContrasena() {
        Usuario lector = usuario(5L, TipoAcceso.LECTOR);
        Usuario admin = usuario(1L, TipoAcceso.ADMINISTRADOR);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(lector));
        when(rolService.paraAsignar(ID_ROL_LECTOR)).thenReturn(lector.getRol());

        UsuarioForm form = formulario(lector, ID_ROL_LECTOR);
        form.setNombre("Nombre Nuevo");
        usuarioAdminService.actualizar(5L, form, admin);

        assertEquals("HASH-ANTERIOR", lector.getContrasena());
        assertEquals("Nombre Nuevo", lector.getNombre());
        verifyNoInteractions(passwordEncoder, sesionesUsuario);
    }

    @Test
    @DisplayName("Un administrador no puede quitarse a sí mismo el acceso de administrador")
    void noSeQuitaAdministracion() {
        Usuario admin = usuario(1L, TipoAcceso.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(rolService.paraAsignar(ID_ROL_LECTOR)).thenReturn(rol(ID_ROL_LECTOR, TipoAcceso.LECTOR));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> usuarioAdminService.actualizar(1L, formulario(admin, ID_ROL_LECTOR), admin));

        assertEquals("rolId", ex.getCampo());
        assertEquals(TipoAcceso.ADMINISTRADOR, admin.getAcceso());
    }

    @Test
    @DisplayName("Un administrador no puede cambiar su propio correo con la sesión iniciada")
    void noCambiaSuPropioCorreo() {
        Usuario admin = usuario(1L, TipoAcceso.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        UsuarioForm form = formulario(admin, ID_ROL_ADMIN);
        form.setCorreo("nuevo@correo.com");
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> usuarioAdminService.actualizar(1L, form, admin));

        assertEquals("correo", ex.getCampo());
    }

    @Test
    @DisplayName("Un administrador no puede desactivar su propia cuenta")
    void noSeDesactivaASiMismo() {
        Usuario admin = usuario(1L, TipoAcceso.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(ReglaNegocioException.class, () -> usuarioAdminService.cambiarEstado(1L, admin));
        assertTrue(admin.isActivo());
        verifyNoInteractions(sesionesUsuario);
    }

    @Test
    @DisplayName("Desactivar una cuenta cierra sus sesiones abiertas")
    void desactivarCierraSesiones() {
        Usuario lector = usuario(5L, TipoAcceso.LECTOR);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(lector));

        usuarioAdminService.cambiarEstado(5L, usuario(1L, TipoAcceso.ADMINISTRADOR));

        assertFalse(lector.isActivo());
        verify(sesionesUsuario).cerrar(lector.getCorreo());
    }

    @Test
    @DisplayName("Cambiar el rol de otro usuario a otro nivel de acceso cierra sus sesiones")
    void cambiarAccesoCierraSesiones() {
        Usuario lector = usuario(5L, TipoAcceso.LECTOR);
        String correo = lector.getCorreo();
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(lector));
        when(rolService.paraAsignar(ID_ROL_ADMIN)).thenReturn(rol(ID_ROL_ADMIN, TipoAcceso.ADMINISTRADOR));

        usuarioAdminService.actualizar(5L, formulario(lector, ID_ROL_ADMIN), usuario(1L, TipoAcceso.ADMINISTRADOR));

        verify(sesionesUsuario).cerrar(correo);
    }

    @Test
    @DisplayName("Rechaza nombres con números o símbolos")
    void rechazaNombreInvalido() {
        UsuarioForm form = formularioNuevo();
        form.setNombre("Ana123");

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> usuarioAdminService.crear(form));

        assertEquals("nombre", ex.getCampo());
    }

    @Test
    @DisplayName("No se puede desactivar al último administrador activo")
    void noDesactivaUltimoAdministrador() {
        Usuario actor = usuario(1L, TipoAcceso.ADMINISTRADOR);
        actor.setActivo(false);
        Usuario otroAdmin = usuario(2L, TipoAcceso.ADMINISTRADOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otroAdmin));
        when(usuarioRepository.contarActivosPorAccesoExcepto(TipoAcceso.ADMINISTRADOR, 2L)).thenReturn(0L);

        assertThrows(ReglaNegocioException.class, () -> usuarioAdminService.cambiarEstado(2L, actor));
        assertTrue(otroAdmin.isActivo());
    }

    @Test
    @DisplayName("No elimina a un usuario que es autor de libros")
    void noEliminaAutor() {
        Usuario escritor = usuario(4L, TipoAcceso.ESCRITOR);
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(escritor));
        when(libroRepository.countByEscritor(escritor)).thenReturn(2L);

        assertThrows(ReglaNegocioException.class,
                () -> usuarioAdminService.eliminar(4L, usuario(1L, TipoAcceso.ADMINISTRADOR)));
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eliminar un usuario borra también su lista de lectura")
    void eliminaUsuarioYSuLista() {
        Usuario lector = usuario(5L, TipoAcceso.LECTOR);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(lector));
        when(libroRepository.countByEscritor(lector)).thenReturn(0L);

        usuarioAdminService.eliminar(5L, usuario(1L, TipoAcceso.ADMINISTRADOR));

        verify(lecturaRepository).deleteByLector(lector);
        verify(usuarioRepository).delete(lector);
        verify(sesionesUsuario).cerrar(lector.getCorreo());
    }

    @Test
    @DisplayName("Nadie puede eliminar su propia cuenta")
    void noSeEliminaASiMismo() {
        Usuario admin = usuario(1L, TipoAcceso.ADMINISTRADOR);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(ReglaNegocioException.class, () -> usuarioAdminService.eliminar(1L, admin));
        verify(usuarioRepository, never()).delete(any());
    }
}
