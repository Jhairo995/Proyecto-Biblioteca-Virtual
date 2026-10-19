package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.Usuario;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(Usuario usuario) {

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
}