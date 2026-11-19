package com.example.ejemplodemo.dto;

import com.example.ejemplodemo.model.Rol;
import com.example.ejemplodemo.model.TipoAcceso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Datos del formulario de roles del panel de administración
@Getter
@Setter
public class RolForm {

    @NotBlank(message = "El nombre del rol es obligatorio.")
    @Size(min = 3, max = Rol.NOMBRE_MAX, message = "El nombre debe tener entre 3 y 50 caracteres.")
    @Pattern(regexp = "^[\\p{L}0-9 ._-]+$",
            message = "El nombre solo admite letras, números, espacios, puntos y guiones.")
    private String nombre;

    @Size(max = Rol.DESCRIPCION_MAX, message = "La descripción admite como máximo 255 caracteres.")
    private String descripcion;

    @NotNull(message = "Selecciona el nivel de acceso del rol.")
    private TipoAcceso acceso;

    public static RolForm desde(Rol rol) {
        RolForm form = new RolForm();
        form.setNombre(rol.getNombre());
        form.setDescripcion(rol.getDescripcion());
        form.setAcceso(rol.getAcceso());
        return form;
    }
}
