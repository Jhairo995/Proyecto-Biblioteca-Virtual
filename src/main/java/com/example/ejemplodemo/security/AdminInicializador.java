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

// Crea el administrador inicial al arrancar, ya que los administradores no pueden registrarse públicamente
@Component
public class AdminInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);
    private static final String DNI_ADMIN = "00000000";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correo;
    private final String password;

    public AdminInicializador(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.admin.correo}") String correo,
                              @Value("${app.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correo = correo;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }
        if (password.isBlank()) {
            log.warn("No se creó el administrador: define la variable de entorno ADMIN_PASSWORD.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setCorreo(correo);
        admin.setDni(DNI_ADMIN);
        admin.setRol(Rol.ADMINISTRADOR);
        admin.setNivelCuenta(NivelCuenta.PREMIUM);
        admin.setContrasena(passwordEncoder.encode(password));
        usuarioRepository.save(admin);

        log.info("Administrador inicial creado: {}", correo);
    }
}
