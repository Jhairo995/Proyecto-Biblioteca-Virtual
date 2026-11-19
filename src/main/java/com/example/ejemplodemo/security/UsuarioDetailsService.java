package com.example.ejemplodemo.security;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Carga los usuarios desde la BD para que Spring Security valide el login (correo + contraseña BCrypt)
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException("El usuario no tiene un rol asignado: " + correo);
        }

        // El rol otorga su nivel de acceso como autoridad (ROLE_LECTOR, ROLE_ESCRITOR o ROLE_ADMINISTRADOR)
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().getAcceso().name())
                .disabled(!usuario.isActivo())
                .build();
    }
}
