package com.example.ejemplodemo.dto;

import com.example.ejemplodemo.model.NivelCuenta;
import com.example.ejemplodemo.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Datos del formulario de usuarios del panel. La contraseña es obligatoria solo al crear (lo valida el servicio)
@Getter
@Setter
public class UsuarioForm {
    public static final int CONTRASENA_MIN = 6;
    // BCrypt solo considera los primeros 72 bytes de la contraseña
    public static final int CONTRASENA_MAX = 72;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    @Pattern(regexp = "^[\\p{L} .'-]+$", message = "El nombre solo admite letras, espacios, apóstrofos y guiones.")
    private String nombre;

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos.")
    private String dni;

    @NotBlank(message = "El correo es obligatorio.")
    @Size(max = 120, message = "El correo admite como máximo 120 caracteres.")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Ingresa un correo electrónico válido.")
    private String correo;

    @Size(min = CONTRASENA_MIN, max = CONTRASENA_MAX, message = "La contraseña debe tener entre 6 y 72 caracteres.")
    private String contrasena;

    private String confirmacion;

    @NotNull(message = "Selecciona un rol.")
    private Long rolId;

    @NotNull(message = "Selecciona el plan de la cuenta.")
    private NivelCuenta nivelCuenta = NivelCuenta.GRATUITO;

    private boolean activo = true;

    // Nunca se copia la contraseña: al editar, vacía significa "mantener la actual"
    public static UsuarioForm desde(Usuario usuario) {
        UsuarioForm form = new UsuarioForm();
        form.setNombre(usuario.getNombre());
        form.setDni(usuario.getDni());
        form.setCorreo(usuario.getCorreo());
        form.setRolId(usuario.getRol() == null ? null : usuario.getRol().getId());
        form.setNivelCuenta(usuario.getNivelCuenta());
        form.setActivo(usuario.isActivo());
        return form;
    }
}
