package com.example.ejemplodemo.security;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.Service.RolService;
import com.example.ejemplodemo.Service.UsuarioService;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Crea el administrador inicial al arrancar, ya que los administradores no pueden registrarse públicamente
// Se ejecuta después de DatosIniciales, que crea los roles del sistema
@Component
@Order(2)
public class AdminInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);
    private static final String DNI_ADMIN = UsuarioService.DNI_RESERVADO_ADMIN;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolService rolService;
    private final String correo;
    private final String password;

    public AdminInicializador(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              RolService rolService,
                              @Value("${app.admin.correo}") String correo,
                              @Value("${app.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolService = rolService;
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
        // Evita que la aplicación no arranque si otro usuario ya ocupa el DNI reservado
        if (usuarioRepository.existsByDni(DNI_ADMIN)) {
            log.error("No se creó el administrador: el DNI reservado {} ya pertenece a otro usuario.", DNI_ADMIN);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setCorreo(correo);
        admin.setDni(DNI_ADMIN);
        admin.setRol(rolService.rolBase(TipoAcceso.ADMINISTRADOR));
        admin.setActivo(true);
        admin.setNivelCuenta(NivelCuenta.PREMIUM);
        admin.setContrasena(passwordEncoder.encode(password));
        usuarioRepository.save(admin);

        log.info("Administrador inicial creado: {}", correo);
    }
}
