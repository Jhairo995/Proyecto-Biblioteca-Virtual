package com.example.ejemplodemo.dto;

import com.example.ejemplodemo.model.Categoria;
import com.example.ejemplodemo.model.ColorPortada;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Datos del formulario de categorías del panel de administración
@Getter
@Setter
public class CategoriaForm {

    @NotBlank(message = "El nombre de la categoría es obligatorio.")
    @Size(min = 2, max = Categoria.NOMBRE_MAX, message = "El nombre debe tener entre 2 y 60 caracteres.")
    @Pattern(regexp = "^[\\p{L}0-9 .,&+#()/'-]+$",
            message = "El nombre contiene caracteres no permitidos.")
    private String nombre;

    @Size(max = Categoria.DESCRIPCION_MAX, message = "La descripción admite como máximo 255 caracteres.")
    private String descripcion;

    @NotBlank(message = "Indica el ícono de la categoría.")
    @Size(max = Categoria.ICONO_MAX, message = "El ícono admite como máximo 40 caracteres.")
    @Pattern(regexp = "^bi-[a-z0-9]+(-[a-z0-9]+)*$",
            message = "Usa el nombre de un ícono de Bootstrap Icons, por ejemplo: bi-database.")
    private String icono = Categoria.ICONO_POR_DEFECTO;

    @NotNull(message = "Selecciona el color de portada.")
    private ColorPortada color = ColorPortada.CIAN;

    private boolean activa = true;

    public static CategoriaForm desde(Categoria categoria) {
        CategoriaForm form = new CategoriaForm();
        form.setNombre(categoria.getNombre());
        form.setDescripcion(categoria.getDescripcion());
        form.setIcono(categoria.getIcono());
        form.setColor(categoria.getColor());
        form.setActiva(categoria.isActiva());
        return form;
    }
}
