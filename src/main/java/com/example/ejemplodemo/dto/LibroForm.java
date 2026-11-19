package com.example.ejemplodemo.dto;

import com.example.ejemplodemo.model.EstadoLibro;
import com.example.ejemplodemo.model.Libro;
import com.example.ejemplodemo.model.NivelCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Datos del formulario de libros del panel. El PDF llega aparte (obligatorio al crear, opcional al editar)
@Getter
@Setter
public class LibroForm {
    public static final int TITULO_MAX = 200;
    public static final int DESCRIPCION_MAX = 1000;
    public static final int IDIOMA_MAX = 50;
    public static final String PATRON_IDIOMA = "^[\\p{L} ]+$";

    @NotBlank(message = "El título es obligatorio.")
    @Size(min = 2, max = TITULO_MAX, message = "El título debe tener entre 2 y 200 caracteres.")
    private String titulo;

    @Size(max = DESCRIPCION_MAX, message = "La descripción admite como máximo 1000 caracteres.")
    private String descripcion;

    @NotNull(message = "Selecciona una categoría.")
    private Long categoriaId;

    @Size(max = IDIOMA_MAX, message = "El idioma admite como máximo 50 caracteres.")
    @Pattern(regexp = PATRON_IDIOMA, message = "El idioma solo admite letras y espacios.")
    private String idioma = "Español";

    @NotNull(message = "Selecciona el nivel de acceso.")
    private NivelCuenta nivelAcceso = NivelCuenta.GRATUITO;

    @NotNull(message = "Selecciona el estado del libro.")
    private EstadoLibro estado = EstadoLibro.APROBADO;

    // Opcional: un libro puede no tener escritor (publicado por la biblioteca)
    private Long escritorId;

    public static LibroForm desde(Libro libro) {
        LibroForm form = new LibroForm();
        form.setTitulo(libro.getTitulo());
        form.setDescripcion(libro.getDescripcion());
        form.setCategoriaId(libro.getCategoria() == null ? null : libro.getCategoria().getId());
        form.setIdioma(libro.getIdioma());
        form.setNivelAcceso(libro.getNivelAcceso());
        form.setEstado(libro.getEstado());
        form.setEscritorId(libro.getEscritor() == null ? null : libro.getEscritor().getId());
        return form;
    }
}
