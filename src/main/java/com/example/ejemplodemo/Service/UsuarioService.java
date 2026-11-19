package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.dto.RegistroUsuario;
import com.example.ejemplodemo.dto.UsuarioForm;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    // DNI reservado para el administrador inicial (ver AdminInicializador)
    public static final String DNI_RESERVADO_ADMIN = "00000000";

    static final Pattern PATRON_DNI = Pattern.compile("\\d{8}");
    static final Pattern PATRON_NOMBRE = Pattern.compile("^[\\p{L} .'-]+$");
    static final int NOMBRE_MIN = 2;
    static final String MENSAJE_NOMBRE = "El nombre debe tener entre 2 y 100 caracteres (solo letras, espacios, apóstrofos y guiones).";
    static final Pattern PATRON_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    static final int NOMBRE_MAX = 100;
    static final int CORREO_MAX = 120;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolService rolService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, RolService rolService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolService = rolService;
    }

    public Usuario registrarUsuario(RegistroUsuario datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Faltan los datos del usuario.");
        }
        String nombre = datos.nombre() == null ? "" : datos.nombre().trim();
        String correo = datos.correo() == null ? "" : datos.correo().trim();
        String dni = datos.dni() == null ? "" : datos.dni().trim();
        validarDatos(nombre, correo, dni, datos.contrasena());

        if (usuarioRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("El DNI ya está registrado en el sistema.");
        }

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setDni(dni);
        // Solo se permite elegir LECTOR o ESCRITOR; los administradores no se registran públicamente
        TipoAcceso acceso = TipoAcceso.ESCRITOR.name().equals(datos.rol()) ? TipoAcceso.ESCRITOR : TipoAcceso.LECTOR;
        usuario.setRol(rolService.rolBase(acceso));
        // Toda cuenta nueva inicia activa y en el plan gratuito
        usuario.setNivelCuenta(NivelCuenta.GRATUITO);
        usuario.setActivo(true);

        // Encriptación irreversible de contraseña antes de persistir
        usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));

        return usuarioRepository.save(usuario);
    }

    // Valida los datos del formulario antes de consultar la BD
    private static void validarDatos(String nombre, String correo, String dni, String contrasena) {
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (!nombreValido(nombre)) {
            throw new IllegalArgumentException(MENSAJE_NOMBRE);
        }
        if (!PATRON_DNI.matcher(dni).matches()) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }
        if (DNI_RESERVADO_ADMIN.equals(dni)) {
            throw new IllegalArgumentException("El DNI ingresado no es válido.");
        }
        if (correo.length() > CORREO_MAX || !PATRON_CORREO.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (!longitudContrasenaValida(contrasena)) {
            throw new IllegalArgumentException("La contraseña debe tener entre " + UsuarioForm.CONTRASENA_MIN
                    + " y " + UsuarioForm.CONTRASENA_MAX + " caracteres.");
        }
    }

    // Misma regla que el formulario del panel (UsuarioForm)
    static boolean nombreValido(String nombre) {
        return nombre.length() >= NOMBRE_MIN && nombre.length() <= NOMBRE_MAX && PATRON_NOMBRE.matcher(nombre).matches();
    }

    // BCrypt ignora lo que pase de 72 bytes, así que se rechaza en lugar de truncar en silencio
    static boolean longitudContrasenaValida(String contrasena) {
        return contrasena != null
                && contrasena.length() >= UsuarioForm.CONTRASENA_MIN
                && contrasena.getBytes(StandardCharsets.UTF_8).length <= UsuarioForm.CONTRASENA_MAX;
    }
}
