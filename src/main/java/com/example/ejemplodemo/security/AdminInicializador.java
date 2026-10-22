package com.example.ejemplodemo.security;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Crea el administrador inicial al arrancar, ya que los administradores no pueden registrarse públicamente
@Component
public class AdminInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correo;
    private final String dni;
    private final String password;

    public AdminInicializador(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.admin.correo}") String correo,
                              @Value("${app.admin.dni}") String dni,
                              @Value("${app.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correo = correo;
        this.dni = dni;
        this.password = password;
    }

    // Si algo impide crear al administrador solo se avisa en el log: la aplicación debe arrancar igual
    @Override
    public void run(String... args) {
        Optional<Usuario> existente = usuarioRepository.findByCorreo(correo);
        if (existente.isPresent()) {
            Rol rol = existente.get().getRol();
            if (rol != Rol.ADMINISTRADOR) {
                log.warn("No se creó el administrador: el correo {} ya pertenece a una cuenta con rol {}.", correo, rol);
            }
            return;
        }
        if (password.isBlank()) {
            log.warn("No se creó el administrador: define la variable de entorno ADMIN_PASSWORD.");
            return;
        }
        if (usuarioRepository.existsByDni(dni)) {
            log.warn("No se creó el administrador: el DNI {} (app.admin.dni) ya pertenece a otra cuenta.", dni);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setCorreo(correo);
        admin.setDni(dni);
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setNivelCuenta(NivelCuenta.PREMIUM);
        admin.setContrasena(passwordEncoder.encode(password));
        usuarioRepository.save(admin);

        log.info("Administrador inicial creado: {}", correo);
    }
}
