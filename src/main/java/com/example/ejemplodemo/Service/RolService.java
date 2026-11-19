package com.example.ejemplodemo.Service;

import com.example.ejemplodemo.Repository.ConteoPorId;
import com.example.ejemplodemo.Repository.RolRepository;
import com.example.ejemplodemo.Repository.UsuarioRepository;
import com.example.ejemplodemo.dto.RolForm;
import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import com.example.ejemplodemo.security.SesionesUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final SesionesUsuario sesionesUsuario;

    public RolService(RolRepository rolRepository, UsuarioRepository usuarioRepository, SesionesUsuario sesionesUsuario) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.sesionesUsuario = sesionesUsuario;
    }

    public List<Rol> listar() {
        return rolRepository.findAllByOrderBySistemaDescNombreAsc();
    }

    public Rol buscar(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado."));
    }

    // Rol del sistema de un nivel de acceso (se crea al arrancar, ver DatosIniciales)
    public Rol rolBase(TipoAcceso acceso) {
        return rolRepository.findFirstByAccesoAndSistemaTrue(acceso)
                .orElseThrow(() -> new IllegalStateException("No existe el rol base " + acceso + "."));
    }

    // Rol elegido en un formulario de usuario
    public Rol paraAsignar(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("rolId", "Selecciona un rol.");
        }
        return rolRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("rolId", "El rol seleccionado no existe."));
    }

    public Map<Long, Long> usuariosPorRol() {
        return usuarioRepository.contarPorRol().stream()
                .filter(conteo -> conteo.getId() != null)
                .collect(Collectors.toUnmodifiableMap(ConteoPorId::getId, ConteoPorId::getTotal));
    }

    @Transactional
    public Rol crear(RolForm form) {
        String nombre = Textos.obligatorio(form.getNombre(), "nombre", "El nombre del rol es obligatorio.");
        validarDatos(nombre, form);
        validarNombreLibre(nombre, null);

        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(Textos.limpiar(form.getDescripcion()));
        rol.setAcceso(form.getAcceso());
        rol.setSistema(false);
        return rolRepository.save(rol);
    }

    @Transactional
    public Rol actualizar(Long id, RolForm form) {
        Rol rol = buscar(id);
        String nombre = Textos.obligatorio(form.getNombre(), "nombre", "El nombre del rol es obligatorio.");
        validarDatos(nombre, form);
        TipoAcceso acceso = form.getAcceso();

        if (rol.isSistema() && !rol.getNombre().equals(nombre)) {
            throw new ReglaNegocioException("nombre", "El nombre de un rol del sistema no se puede cambiar.");
        }
        if (rol.isSistema() && rol.getAcceso() != acceso) {
            throw new ReglaNegocioException("acceso", "El nivel de acceso de un rol del sistema no se puede cambiar.");
        }
        validarNombreLibre(nombre, id);
        if (rol.getAcceso() == TipoAcceso.ADMINISTRADOR && acceso != TipoAcceso.ADMINISTRADOR
                && usuarioRepository.contarActivosPorAccesoFueraDeRol(TipoAcceso.ADMINISTRADOR, rol) == 0) {
            throw new ReglaNegocioException("acceso", "El cambio dejaría la plataforma sin administradores activos.");
        }

        boolean cambiaAcceso = rol.getAcceso() != acceso;
        rol.setNombre(nombre);
        rol.setDescripcion(Textos.limpiar(form.getDescripcion()));
        rol.setAcceso(acceso);
        // Quienes tienen este rol deben volver a iniciar sesión para recibir el nuevo nivel de acceso
        if (cambiaAcceso) {
            usuarioRepository.findByRol(rol).forEach(usuario -> sesionesUsuario.cerrar(usuario.getCorreo()));
        }
        return rol;
    }

    @Transactional
    public Rol eliminar(Long id) {
        Rol rol = buscar(id);
        if (rol.isSistema()) {
            throw new ReglaNegocioException("Los roles del sistema no se pueden eliminar.");
        }
        long asignados = usuarioRepository.countByRol(rol);
        if (asignados > 0) {
            throw new ReglaNegocioException("No se puede eliminar «" + rol.getNombre() + "»: tiene " + asignados
                    + " usuario(s) asignado(s). Reasígnalos a otro rol antes de eliminarlo.");
        }
        rolRepository.delete(rol);
        return rol;
    }

    private static void validarDatos(String nombre, RolForm form) {
        Textos.longitudMaxima(nombre, Rol.NOMBRE_MAX, "nombre", "El nombre admite como máximo 50 caracteres.");
        Textos.longitudMaxima(form.getDescripcion(), Rol.DESCRIPCION_MAX, "descripcion",
                "La descripción admite como máximo 255 caracteres.");
        if (form.getAcceso() == null) {
            throw new ReglaNegocioException("acceso", "Selecciona el nivel de acceso del rol.");
        }
    }

    private void validarNombreLibre(String nombre, Long id) {
        boolean ocupado = id == null
                ? rolRepository.existsByNombreIgnoreCase(nombre)
                : rolRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (ocupado) {
            throw new ReglaNegocioException("nombre", "Ya existe un rol con el nombre «" + nombre + "».");
        }
    }
}
