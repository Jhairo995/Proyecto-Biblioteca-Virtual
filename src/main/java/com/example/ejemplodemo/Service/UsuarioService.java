package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern PATRON_DNI = Pattern.compile("\\d{8}");
    private static final Pattern PATRON_CORREO = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    // Largo de las columnas VARCHAR(255) que genera Hibernate
    private static final int LONGITUD_MAXIMA_TEXTO = 255;
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;
    // BCrypt no admite contraseñas de más de 72 bytes
    private static final int BYTES_MAXIMOS_CONTRASENA = 72;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correoAdmin;
    private final String dniAdmin;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.admin.correo}") String correoAdmin,
                          @Value("${app.admin.dni}") String dniAdmin) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoAdmin = correoAdmin;
        this.dniAdmin = dniAdmin;
    }

    // Datos que el visitante envía desde el formulario de registro
    public record DatosRegistro(String nombre, String dni, String correo, String contrasena, Rol rol) {
    }

    public Usuario registrarUsuario(DatosRegistro datos) {
        String nombre = recortar(datos.nombre());
        String dni = recortar(datos.dni());
        String correo = recortar(datos.correo());
        validarDatos(nombre, dni, correo, datos.contrasena());

        // El correo y el DNI del administrador quedan reservados aunque aún no se haya creado
        if (dni.equals(dniAdmin) || correo.equalsIgnoreCase(correoAdmin)) {
            throw new IllegalArgumentException("Ese correo o DNI está reservado para el administrador.");
        }
        if (usuarioRepository.existsByDni(dni)) {
            throw new IllegalArgumentException("El DNI ya está registrado en el sistema.");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya está registrado en el sistema.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setDni(dni);
        usuario.setCorreo(correo);
        // Solo se permite elegir LECTOR o ESCRITOR; los administradores no se registran públicamente
        usuario.setRol(datos.rol() == Rol.ESCRITOR ? Rol.ESCRITOR : Rol.LECTOR);
        // Toda cuenta nueva inicia en el plan gratuito
        usuario.setNivelCuenta(NivelCuenta.GRATUITO);
        // Encriptación irreversible de contraseña antes de persistir
        usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));

        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            // Dos registros simultáneos con el mismo correo o DNI: la clave UNIQUE de la BD rechaza el segundo
            throw new IllegalArgumentException("El correo o el DNI ya está registrado en el sistema.");
        }
    }

    private static String recortar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    // El formulario ya valida esto en el navegador, pero la API puede recibir peticiones directas
    private static void validarDatos(String nombre, String dni, String correo, String contrasena) {
        if (nombre.isEmpty() || nombre.length() > LONGITUD_MAXIMA_TEXTO) {
            throw new IllegalArgumentException("El nombre es obligatorio (máximo " + LONGITUD_MAXIMA_TEXTO + " caracteres).");
        }
        if (!PATRON_DNI.matcher(dni).matches()) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }
        if (correo.length() > LONGITUD_MAXIMA_TEXTO || !PATRON_CORREO.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
        }
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña no puede superar los " + BYTES_MAXIMOS_CONTRASENA + " caracteres.");
        }
    }
}
