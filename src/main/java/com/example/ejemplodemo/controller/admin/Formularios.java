package com.example.ejemplodemo.controller.admin;

import com.example.ejemplodemo.Service.ReglaNegocioException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.BindingResult;

// Utilidades compartidas por los formularios del panel
final class Formularios {

    static final int TAMANO_PAGINA = 10;
    static final int BUSQUEDA_MAX = 100;
    static final String MENSAJE_EN_USO = "No se pudo eliminar porque otros registros todavía dependen de él. Actualiza la página e inténtalo de nuevo.";
    static final String MENSAJE_DUPLICADO = "Otro registro ya usa esos datos. Revisa los campos únicos e inténtalo de nuevo.";

    private Formularios() {
    }

    // Muestra una regla de negocio junto al campo afectado, o como error general del formulario
    static void registrar(BindingResult resultado, ReglaNegocioException error) {
        String campo = error.getCampo();
        Object formulario = resultado.getTarget();
        if (campo != null && formulario != null && new BeanWrapperImpl(formulario).isReadableProperty(campo)) {
            resultado.rejectValue(campo, "regla", error.getMessage());
        } else {
            resultado.reject("regla", error.getMessage());
        }
    }

    // Texto de búsqueda recortado a una longitud razonable
    static String busqueda(String texto) {
        if (texto == null) {
            return null;
        }
        return texto.length() > BUSQUEDA_MAX ? texto.substring(0, BUSQUEDA_MAX) : texto;
    }

    static int pagina(int solicitada) {
        return Math.max(solicitada, 0);
    }
}
