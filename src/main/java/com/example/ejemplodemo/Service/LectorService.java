package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.LecturaRepository;
import com.example.ejemplodemo.Repository.LibroRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Lectura;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LectorService {

    private final LibroRepository libroRepository;
    private final LecturaRepository lecturaRepository;
    private final UsuarioRepository usuarioRepository;

    public LectorService(LibroRepository libroRepository,
                         LecturaRepository lecturaRepository,
                         UsuarioRepository usuarioRepository) {
        this.libroRepository = libroRepository;
        this.lecturaRepository = lecturaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Solo los libros aprobados por un administrador son visibles para los lectores
    public List<Libro> catalogo() {
        return libroRepository.findByEstadoOrderByFechaRegistroDesc(EstadoLibro.APROBADO);
    }

    public List<Lectura> miLista(Usuario lector) {
        return lecturaRepository.findByLectorOrderByFechaAgregadoDesc(lector);
    }

    public Set<Long> idsEnLista(Usuario lector) {
        return miLista(lector).stream()
                .map(lectura -> lectura.getLibro().getId())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Libro libroPublicado(Long id) {
        return libroRepository.findById(id)
                .filter(libro -> libro.getEstado() == EstadoLibro.APROBADO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado."));
    }

    // Los libros Premium solo los leen cuentas Premium
    public boolean puedeLeer(Usuario lector, Libro libro) {
        return libro.getNivelAcceso() == NivelCuenta.GRATUITO || lector.getNivelCuenta() == NivelCuenta.PREMIUM;
    }

    public Libro agregarALista(Usuario lector, Long libroId) {
        Libro libro = libroPublicado(libroId);
        if (!lecturaRepository.existsByLectorAndLibro(lector, libro)) {
            Lectura lectura = new Lectura();
            lectura.setLector(lector);
            lectura.setLibro(libro);
            lecturaRepository.save(lectura);
        }
        return libro;
    }

    @Transactional
    public Libro quitarDeLista(Usuario lector, Long libroId) {
        Libro libro = libroPublicado(libroId);
        lecturaRepository.deleteByLectorAndLibro(lector, libro);
        return libro;
    }

    // Suscripción simulada (proyecto académico: no hay pasarela de pago)
    @Transactional
    public void activarPremium(Usuario lector) {
        Usuario usuario = usuarioRepository.findById(lector.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        usuario.setNivelCuenta(NivelCuenta.PREMIUM);
    }
}
