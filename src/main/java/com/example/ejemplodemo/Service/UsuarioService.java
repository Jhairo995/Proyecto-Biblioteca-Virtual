package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UsuarioService {

    // DNI reservado para el administrador inicial (ver AdminInicializador)
    public static final String DNI_RESERVADO_ADMIN = "00000000";

    private static final Pattern PATRON_DNI = Pattern.compile("\\d{8}");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Faltan los datos del usuario.");
        }
        validarDatos(usuario);

        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new IllegalArgumentException("El DNI ya está registrado en el sistema.");
        }

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema.");
        }

        // Solo se permite elegir LECTOR o ESCRITOR; los administradores no se registran públicamente
        usuario.setId(null);
        usuario.setRol(usuario.getRol() == Rol.ESCRITOR ? Rol.ESCRITOR : Rol.LECTOR);
        // Toda cuenta nueva inicia en el plan gratuito
        usuario.setNivelCuenta(NivelCuenta.GRATUITO);

        // Encriptación irreversible de contraseña antes de persistir
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        return usuarioRepository.save(usuario);
    }

    // Valida y normaliza los datos del formulario antes de consultar la BD
    private void validarDatos(Usuario usuario) {
        String nombre = usuario.getNombre() == null ? "" : usuario.getNombre().trim();
        String correo = usuario.getCorreo() == null ? "" : usuario.getCorreo().trim();
        String dni = usuario.getDni() == null ? "" : usuario.getDni().trim();
        String contrasena = usuario.getContrasena();

        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (!PATRON_DNI.matcher(dni).matches()) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }
        if (DNI_RESERVADO_ADMIN.equals(dni)) {
            throw new IllegalArgumentException("El DNI ingresado no es válido.");
        }
        if (!PATRON_CORREO.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setDni(dni);
    }
}
