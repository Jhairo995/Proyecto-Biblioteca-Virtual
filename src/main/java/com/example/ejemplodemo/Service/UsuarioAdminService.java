package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.LecturaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.dto.UsuarioForm;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.security.SesionesUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

// Gestión de usuarios desde el panel de administración
@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final LecturaRepository lecturaRepository;
    private final RolService rolService;
    private final PasswordEncoder passwordEncoder;
    private final SesionesUsuario sesionesUsuario;

    public UsuarioAdminService(UsuarioRepository usuarioRepository,
                               LibroRepository libroRepository,
                               LecturaRepository lecturaRepository,
                               RolService rolService,
                               PasswordEncoder passwordEncoder,
                               SesionesUsuario sesionesUsuario) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.lecturaRepository = lecturaRepository;
        this.rolService = rolService;
        this.passwordEncoder = passwordEncoder;
        this.sesionesUsuario = sesionesUsuario;
    }

    public Page<Usuario> buscar(String texto, Long rolId, Boolean activo, Pageable pageable) {
        return usuarioRepository.buscar(Textos.patronBusqueda(texto), rolId, activo, pageable);
    }

    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }

    public long contarTodos() {
        return usuarioRepository.count();
    }

    public long contarActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    public List<Libro> librosDe(Usuario usuario) {
        return libroRepository.findByEscritorOrderByFechaRegistroDesc(usuario);
    }

    public long lecturasDe(Usuario usuario) {
        return lecturaRepository.countByLector(usuario);
    }

    @Transactional
    public Usuario crear(UsuarioForm form) {
        Datos datos = normalizar(form);
        if (form.getContrasena() == null || form.getContrasena().isEmpty()) {
            throw new ReglaNegocioException("contrasena", "La contraseña es obligatoria.");
        }
        validarContrasena(form);
        validarDni(datos.dni(), null);
        validarCorreo(datos.correo(), null);
        Rol rol = rolService.paraAsignar(form.getRolId());

        Usuario usuario = new Usuario();
        aplicar(usuario, datos, rol, form);
        usuario.setContrasena(passwordEncoder.encode(form.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, UsuarioForm form, Usuario actor) {
        Usuario usuario = buscar(id);
        Datos datos = normalizar(form);
        validarContrasena(form);
        validarDni(datos.dni(), usuario);
        validarCorreo(datos.correo(), usuario.getId());
        // La sesión se identifica por el correo: cambiar el propio correo cerraría la sesión actual
        if (esMismo(usuario, actor) && !usuario.getCorreo().equalsIgnoreCase(datos.correo())) {
            throw new ReglaNegocioException("correo", "No puedes cambiar tu propio correo mientras tienes la sesión iniciada.");
        }
        Rol rol = rolService.paraAsignar(form.getRolId());
        protegerAdministradores(usuario, rol.getAcceso(), form.isActivo(), actor);

        // Datos con los que el usuario inició sesión: si cambian, sus sesiones abiertas dejan de ser válidas
        String correoAnterior = usuario.getCorreo();
        boolean cambiaAcceso = usuario.getAcceso() != rol.getAcceso();
        boolean seDesactiva = usuario.isActivo() && !form.isActivo();
        boolean cambiaContrasena = form.getContrasena() != null && !form.getContrasena().isEmpty();

        aplicar(usuario, datos, rol, form);
        if (cambiaContrasena) {
            usuario.setContrasena(passwordEncoder.encode(form.getContrasena()));
        }
        boolean cambiaCorreo = !correoAnterior.equalsIgnoreCase(usuario.getCorreo());
        if (!esMismo(usuario, actor) && (cambiaAcceso || seDesactiva || cambiaContrasena || cambiaCorreo)) {
            sesionesUsuario.cerrar(correoAnterior);
        }
        return usuario;
    }

    @Transactional
    public Usuario cambiarEstado(Long id, Usuario actor) {
        Usuario usuario = buscar(id);
        boolean nuevoEstado = !usuario.isActivo();
        protegerAdministradores(usuario, usuario.getAcceso(), nuevoEstado, actor);
        usuario.setActivo(nuevoEstado);
        if (!nuevoEstado) {
            sesionesUsuario.cerrar(usuario.getCorreo());
        }
        return usuario;
    }

    @Transactional
    public Usuario eliminar(Long id, Usuario actor) {
        Usuario usuario = buscar(id);
        if (esMismo(usuario, actor)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta.");
        }
        if (esAdministradorActivo(usuario) && sinOtrosAdministradores(usuario)) {
            throw new ReglaNegocioException("Debe quedar al menos un administrador activo en la plataforma.");
        }
        long libros = libroRepository.countByEscritor(usuario);
        if (libros > 0) {
            throw new ReglaNegocioException("No se puede eliminar a «" + usuario.getNombre() + "»: es autor de " + libros
                    + " libro(s). Reasigna sus libros o desactiva la cuenta.");
        }
        lecturaRepository.deleteByLector(usuario);
        usuarioRepository.delete(usuario);
        sesionesUsuario.cerrar(usuario.getCorreo());
        return usuario;
    }

    // Impide que el administrador se bloquee a sí mismo o deje la plataforma sin administradores activos
    private void protegerAdministradores(Usuario usuario, TipoAcceso nuevoAcceso, boolean nuevoActivo, Usuario actor) {
        if (esMismo(usuario, actor) && nuevoAcceso != TipoAcceso.ADMINISTRADOR) {
            throw new ReglaNegocioException("rolId", "No puedes quitarte el acceso de administrador a ti mismo.");
        }
        if (esMismo(usuario, actor) && !nuevoActivo) {
            throw new ReglaNegocioException("activo", "No puedes desactivar tu propia cuenta.");
        }
        boolean pierdeAdministracion = esAdministradorActivo(usuario)
                && (nuevoAcceso != TipoAcceso.ADMINISTRADOR || !nuevoActivo);
        if (pierdeAdministracion && sinOtrosAdministradores(usuario)) {
            throw new ReglaNegocioException("rolId", "Debe quedar al menos un administrador activo en la plataforma.");
        }
    }

    private static boolean esAdministradorActivo(Usuario usuario) {
        return usuario.isActivo() && usuario.getAcceso() == TipoAcceso.ADMINISTRADOR;
    }

    private boolean sinOtrosAdministradores(Usuario usuario) {
        return usuarioRepository.contarActivosPorAccesoExcepto(TipoAcceso.ADMINISTRADOR, usuario.getId()) == 0;
    }

    private static boolean esMismo(Usuario usuario, Usuario actor) {
        return actor != null && Objects.equals(usuario.getId(), actor.getId());
    }

    // La contraseña es opcional al editar; si se escribe, debe ser válida y coincidir con la confirmación
    private static void validarContrasena(UsuarioForm form) {
        String contrasena = form.getContrasena();
        if (contrasena == null || contrasena.isEmpty()) {
            return;
        }
        if (!UsuarioService.longitudContrasenaValida(contrasena)) {
            throw new ReglaNegocioException("contrasena", "La contraseña debe tener entre "
                    + UsuarioForm.CONTRASENA_MIN + " y " + UsuarioForm.CONTRASENA_MAX + " caracteres.");
        }
        if (!contrasena.equals(form.getConfirmacion())) {
            throw new ReglaNegocioException("confirmacion", "Las contraseñas no coinciden.");
        }
    }

    private void validarDni(String dni, Usuario actual) {
        if (!UsuarioService.PATRON_DNI.matcher(dni).matches()) {
            throw new ReglaNegocioException("dni", "El DNI debe tener exactamente 8 dígitos.");
        }
        boolean esSuDni = actual != null && dni.equals(actual.getDni());
        if (UsuarioService.DNI_RESERVADO_ADMIN.equals(dni) && !esSuDni) {
            throw new ReglaNegocioException("dni", "El DNI " + dni + " está reservado por el sistema.");
        }
        boolean ocupado = actual == null
                ? usuarioRepository.existsByDni(dni)
                : usuarioRepository.existsByDniAndIdNot(dni, actual.getId());
        if (ocupado) {
            throw new ReglaNegocioException("dni", "El DNI ya está registrado por otro usuario.");
        }
    }

    private void validarCorreo(String correo, Long id) {
        if (correo.length() > UsuarioService.CORREO_MAX || !UsuarioService.PATRON_CORREO.matcher(correo).matches()) {
            throw new ReglaNegocioException("correo", "Ingresa un correo electrónico válido.");
        }
        boolean ocupado = id == null
                ? usuarioRepository.existsByCorreoIgnoreCase(correo)
                : usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id);
        if (ocupado) {
            throw new ReglaNegocioException("correo", "El correo ya está registrado por otro usuario.");
        }
    }

    private record Datos(String nombre, String dni, String correo) {
    }

    private static Datos normalizar(UsuarioForm form) {
        String nombre = Textos.obligatorio(form.getNombre(), "nombre", "El nombre es obligatorio.");
        if (!UsuarioService.nombreValido(nombre)) {
            throw new ReglaNegocioException("nombre", UsuarioService.MENSAJE_NOMBRE);
        }
        return new Datos(
                nombre,
                Textos.obligatorio(form.getDni(), "dni", "El DNI es obligatorio."),
                Textos.obligatorio(form.getCorreo(), "correo", "El correo es obligatorio."));
    }

    private static void aplicar(Usuario usuario, Datos datos, Rol rol, UsuarioForm form) {
        usuario.setNombre(datos.nombre());
        usuario.setDni(datos.dni());
        usuario.setCorreo(datos.correo());
        usuario.setRol(rol);
        usuario.setNivelCuenta(form.getNivelCuenta() == null ? NivelCuenta.GRATUITO : form.getNivelCuenta());
        usuario.setActivo(form.isActivo());
    }
}
