// Comportamiento del panel de administración: confirmaciones, validación en el navegador y vistas previas.
// La validación real ocurre en el servidor; esto solo adelanta los errores al usuario.
document.addEventListener("DOMContentLoaded", () => {

    const TAMANO_MAXIMO_PDF = 20 * 1024 * 1024;

    // ==========================================
    // CONFIRMACIÓN DE ELIMINACIÓN
    // ==========================================
    const modalEliminar = document.getElementById("modalEliminar");
    if (modalEliminar) {
        modalEliminar.addEventListener("show.bs.modal", (evento) => {
            const boton = evento.relatedTarget;
            if (!boton) {
                return;
            }
            modalEliminar.querySelector("form").action = boton.dataset.accion;
            modalEliminar.querySelector("[data-modal-nombre]").textContent = boton.dataset.nombre || "este registro";
            modalEliminar.querySelector("[data-modal-detalle]").textContent =
                boton.dataset.detalle || "Esta acción no se puede deshacer.";
        });
    }

    // ==========================================
    // VALIDACIÓN DE FORMULARIOS
    // ==========================================
    const validarConfirmacion = (campo) => {
        const original = document.getElementById(campo.dataset.confirma);
        const coincide = !original || original.value === campo.value;
        campo.setCustomValidity(coincide ? "" : "Las contraseñas no coinciden.");
    };

    const validarPdf = (campo) => {
        const archivo = campo.files && campo.files[0];
        let mensaje = "";
        if (archivo && !archivo.name.toLowerCase().endsWith(".pdf")) {
            mensaje = "Solo se permiten archivos PDF.";
        } else if (archivo && archivo.size > TAMANO_MAXIMO_PDF) {
            mensaje = "El archivo supera el tamaño máximo permitido (20 MB).";
        }
        campo.setCustomValidity(mensaje);
        const ayuda = campo.parentElement.querySelector(".invalid-feedback");
        if (ayuda && mensaje) {
            ayuda.textContent = mensaje;
        }
    };

    document.querySelectorAll("form.needs-validation").forEach((formulario) => {
        // Al corregir un campo marcado por el servidor se quita la marca de error
        formulario.querySelectorAll(".is-invalid").forEach((campo) => {
            const limpiar = () => campo.classList.remove("is-invalid");
            campo.addEventListener("input", limpiar, { once: true });
            campo.addEventListener("change", limpiar, { once: true });
        });

        formulario.querySelectorAll("[data-confirma]").forEach((campo) => {
            const original = document.getElementById(campo.dataset.confirma);
            campo.addEventListener("input", () => validarConfirmacion(campo));
            if (original) {
                original.addEventListener("input", () => validarConfirmacion(campo));
            }
        });

        formulario.querySelectorAll("input[type=file][data-pdf]").forEach((campo) => {
            campo.addEventListener("change", () => validarPdf(campo));
        });

        formulario.addEventListener("submit", (evento) => {
            formulario.querySelectorAll("[data-confirma]").forEach(validarConfirmacion);
            if (!formulario.checkValidity()) {
                evento.preventDefault();
                evento.stopPropagation();
                const primerError = formulario.querySelector(":invalid");
                if (primerError) {
                    primerError.focus();
                }
            }
            formulario.classList.add("was-validated");
        });
    });

    // ==========================================
    // CONTADORES DE CARACTERES
    // ==========================================
    document.querySelectorAll("[data-contador]").forEach((campo) => {
        const contador = document.getElementById(campo.dataset.contador);
        const maximo = campo.getAttribute("maxlength");
        if (!contador || !maximo) {
            return;
        }
        const actualizar = () => {
            contador.textContent = `${campo.value.length}/${maximo}`;
        };
        campo.addEventListener("input", actualizar);
        actualizar();
    });

    // ==========================================
    // VISTA PREVIA DE CATEGORÍA (ícono, color y nombre)
    // ==========================================
    const vistaPrevia = document.getElementById("vistaPreviaCategoria");
    if (vistaPrevia) {
        const portada = vistaPrevia.querySelector(".categoria-cover");
        const icono = vistaPrevia.querySelector("[data-preview-icono]");
        const nombre = vistaPrevia.querySelector("[data-preview-nombre]");
        const descripcion = vistaPrevia.querySelector("[data-preview-descripcion]");
        const campoIcono = document.getElementById("icono");
        const campoNombre = document.getElementById("nombre");
        const campoDescripcion = document.getElementById("descripcion");
        const campoActiva = document.getElementById("activa");
        const patronIcono = /^bi-[a-z0-9]+(-[a-z0-9]+)*$/;

        const actualizarVista = () => {
            const valorIcono = campoIcono.value.trim();
            icono.className = "bi " + (patronIcono.test(valorIcono) ? valorIcono : "bi-question-circle");
            nombre.textContent = campoNombre.value.trim() || "Nombre de la categoría";
            descripcion.textContent = campoDescripcion.value.trim() || "La descripción aparecerá aquí.";
            const color = document.querySelector("input[name=color]:checked");
            portada.className = "categoria-cover " + (color ? color.dataset.clase : "cover-cyan");
            portada.classList.toggle("inactiva", Boolean(campoActiva) && !campoActiva.checked);
        };

        [campoIcono, campoNombre, campoDescripcion, campoActiva].forEach((campo) => {
            if (campo) {
                campo.addEventListener("input", actualizarVista);
                campo.addEventListener("change", actualizarVista);
            }
        });
        document.querySelectorAll("input[name=color]").forEach((opcion) => opcion.addEventListener("change", actualizarVista));
        actualizarVista();
    }

    // ==========================================
    // FILTROS: aplicar al cambiar una lista desplegable
    // ==========================================
    document.querySelectorAll("form[data-autoenvio] select").forEach((lista) => {
        lista.addEventListener("change", () => lista.form.requestSubmit());
    });
});
