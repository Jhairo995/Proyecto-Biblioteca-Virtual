package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.ResumenAutor;
import com.example.ejemplodemo.model.Rol;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutorService {

    private final UsuarioRepository usuarioRepository;

    public AutorService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Todos los escritores; solo cuentan los libros publicados (aprobados por el administrador)
    public List<ResumenAutor> autores() {
        return usuarioRepository.resumenAutores(Rol.ESCRITOR, EstadoLibro.APROBADO);
    }
}
