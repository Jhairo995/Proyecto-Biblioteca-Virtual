// ==========================================
// AVISOS (toasts)
// ==========================================

const TIPOS_AVISO = {
    success: { icono: "bi-check-circle-fill", titulo: "¡Listo!" },
    danger: { icono: "bi-x-octagon-fill", titulo: "Algo salió mal" },
    warning: { icono: "bi-exclamation-triangle-fill", titulo: "Atención" },
    info: { icono: "bi-info-circle-fill", titulo: "Información" }
};
const DURACION_AVISO_MS = 5000;
const DURACION_AVISO_ERROR_MS = 8000;

// Muestra un aviso flotante; las vistas también la usan (p. ej. el registro en login.html)
function mostrarAviso(mensaje, tipo = "info") {
    const clave = TIPOS_AVISO[tipo] ? tipo : "info";
    const { icono, titulo } = TIPOS_AVISO[clave];

    const aviso = document.createElement("div");
    aviso.className = `toast aviso aviso-${clave}`;
    aviso.setAttribute("role", clave === "danger" ? "alert" : "status");
    aviso.setAttribute("aria-atomic", "true");
    aviso.innerHTML = `
        <div class="d-flex align-items-start gap-3 p-3">
            <i class="bi ${icono} aviso-icono" aria-hidden="true"></i>
            <div class="flex-grow-1">
                <strong class="d-block aviso-titulo"></strong>
                <span class="aviso-texto"></span>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Cerrar"></button>
        </div>`;
    // textContent: el mensaje nunca se interpreta como HTML
    aviso.querySelector(".aviso-titulo").textContent = titulo;
    aviso.querySelector(".aviso-texto").textContent = mensaje;

    contenedorAvisos().append(aviso);
    aviso.addEventListener("hidden.bs.toast", () => aviso.remove());
    bootstrap.Toast.getOrCreateInstance(aviso, {
        delay: clave === "danger" ? DURACION_AVISO_ERROR_MS : DURACION_AVISO_MS
    }).show();
}

function contenedorAvisos() {
    let contenedor = document.getElementById("avisos");
    if (!contenedor) {
        contenedor = document.createElement("div");
        contenedor.id = "avisos";
        contenedor.className = "toast-container position-fixed avisos";
        document.body.append(contenedor);
    }
    return contenedor;
}


// ==========================================
// BOTONES CON ESTADO "CARGANDO" (data-cargando="Texto…")
// ==========================================

function ponerCargando(boton, cargando) {
    if (cargando) {
        boton.dataset.textoOriginal = boton.innerHTML;
        boton.disabled = true;
        boton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span><span role="status"></span>';
        boton.lastElementChild.textContent = boton.dataset.cargando || "Procesando…";
    } else if (boton.dataset.textoOriginal !== undefined) {
        boton.innerHTML = boton.dataset.textoOriginal;
        boton.disabled = false;
        delete boton.dataset.textoOriginal;
    }
}


// ==========================================
// DIÁLOGO DE CONFIRMACIÓN (formularios con data-confirmar)
// ==========================================

function modalConfirmar() {
    let modal = document.getElementById("modalConfirmar");
    if (modal) {
        return modal;
    }
    modal = document.createElement("div");
    modal.id = "modalConfirmar";
    modal.className = "modal fade modal-confirmar";
    modal.tabIndex = -1;
    modal.setAttribute("aria-labelledby", "modalConfirmarTitulo");
    modal.setAttribute("aria-hidden", "true");
    modal.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-body text-center p-4">
                    <span class="confirmar-icono"><i class="bi bi-exclamation-triangle" aria-hidden="true"></i></span>
                    <h5 class="mt-3 mb-2" id="modalConfirmarTitulo">¿Estás seguro?</h5>
                    <p class="text-muted mb-0" data-confirmar-texto></p>
                </div>
                <div class="modal-footer border-0 pt-0 px-4 pb-4 flex-nowrap">
                    <button type="button" class="btn btn-light w-50" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn btn-danger w-50" data-confirmar-aceptar></button>
                </div>
            </div>
        </div>`;
    document.body.append(modal);
    return modal;
}

function pedirConfirmacion(form, submitter) {
    const modal = modalConfirmar();
    modal.querySelector("[data-confirmar-texto]").textContent = form.dataset.confirmar;
    const aceptar = modal.querySelector("[data-confirmar-aceptar]");
    aceptar.textContent = form.dataset.confirmarBoton || "Sí, continuar";
    aceptar.onclick = () => {
        form.dataset.confirmado = "true";
        bootstrap.Modal.getInstance(modal).hide();
        form.requestSubmit(submitter);
    };
    bootstrap.Modal.getOrCreateInstance(modal).show();
}


// ==========================================
// ENVÍO DE FORMULARIOS
// Los dos primeros se registran en fase de captura: corren antes que los
// manejadores propios de cada formulario (p. ej. el registro con fetch)
// ==========================================

// 1. Validación con mensajes de Bootstrap (class="needs-validation" + novalidate)
document.addEventListener("submit", (event) => {
    const form = event.target;
    if (!form.classList.contains("needs-validation") || form.checkValidity()) {
        return;
    }
    event.preventDefault();
    event.stopImmediatePropagation();
    form.classList.add("was-validated");
    form.querySelector(":invalid")?.focus();
}, true);

// 2. Confirmación antes de acciones destructivas
document.addEventListener("submit", (event) => {
    const form = event.target;
    if (form.dataset.confirmar === undefined) {
        return;
    }
    if (form.dataset.confirmado) {
        delete form.dataset.confirmado;
        return;
    }
    event.preventDefault();
    event.stopImmediatePropagation();
    pedirConfirmacion(form, event.submitter);
}, true);

// 3. Spinner en el botón mientras el servidor responde
document.addEventListener("submit", (event) => {
    if (event.defaultPrevented) {
        return;
    }
    const boton = event.submitter ?? event.target.querySelector('[type="submit"]');
    if (boton?.dataset.cargando !== undefined) {
        // Se desactiva después de que el navegador arme los datos del envío
        setTimeout(() => ponerCargando(boton, true));
    }
});

// Al volver con "Atrás" el navegador puede restaurar la página con los botones aún desactivados
window.addEventListener("pageshow", (event) => {
    if (event.persisted) {
        document.querySelectorAll("[data-texto-original]").forEach(boton => ponerCargando(boton, false));
    }
});


document.addEventListener("DOMContentLoaded", () => {

    // ==========================================
    // MENSAJES DEL SERVIDOR (elementos con data-aviso)
    // ==========================================

    document.querySelectorAll("[data-aviso]").forEach(elemento => {
        mostrarAviso(elemento.textContent.trim(), elemento.dataset.aviso);
        elemento.remove();
    });


    // ==========================================
    // MENÚ NAVBAR
    // ==========================================

    const navbar = document.querySelector(".navbar-glass");
    const menu = document.querySelector("#menuPrincipal");
    const navbarToggler = document.querySelector(".navbar-toggler");

    // Cerrar el menú en dispositivos móviles al elegir una opción
    document.querySelectorAll(".navbar-nav .nav-link").forEach(link => {
        link.addEventListener("click", () => {
            if (menu && menu.classList.contains("show") && navbarToggler) {
                navbarToggler.click();
            }
        });
    });

    // Sombra en el navbar al hacer scroll
    const actualizarNavbar = () => {
        if (navbar) {
            navbar.classList.toggle("scrolled", window.scrollY > 10);
        }
    };
    window.addEventListener("scroll", actualizarNavbar, { passive: true });
    actualizarNavbar();


    // ==========================================
    // BOTONES "LEER" (solo tarjetas de muestra sin enlace real)
    // ==========================================

    document.querySelectorAll('.book-card a.btn-primary[href="#"]').forEach(boton => {
        boton.addEventListener("click", (event) => {
            event.preventDefault();

            const tarjeta = boton.closest(".book-card");
            const titulo = tarjeta.querySelector(".card-title").textContent.trim();

            mostrarAviso(`«${titulo}» es un libro de muestra y todavía no está disponible para leer.`, "info");
        });
    });


    // ==========================================
    // MOSTRAR / OCULTAR CONTRASEÑA (data-ver-clave="idDelCampo")
    // ==========================================

    document.querySelectorAll("[data-ver-clave]").forEach(boton => {
        const campo = document.getElementById(boton.dataset.verClave);
        boton.addEventListener("click", () => {
            const mostrar = campo.type === "password";
            campo.type = mostrar ? "text" : "password";
            boton.querySelector("i").className = mostrar ? "bi bi-eye-slash" : "bi bi-eye";
            boton.setAttribute("aria-label", mostrar ? "Ocultar contraseña" : "Mostrar contraseña");
            boton.setAttribute("aria-pressed", String(mostrar));
        });
    });


    // ==========================================
    // CONTADOR DE CARACTERES (data-contador="idDelContador")
    // ==========================================

    document.querySelectorAll("[data-contador]").forEach(campo => {
        const contador = document.getElementById(campo.dataset.contador);
        const actualizar = () => {
            contador.textContent = `${campo.value.length} / ${campo.maxLength}`;
        };
        campo.addEventListener("input", actualizar);
        actualizar();
    });


    // ==========================================
    // ZONA PARA SUBIR ARCHIVOS PDF (.zona-archivo)
    // ==========================================

    const BYTES_POR_MB = 1024 * 1024;

    document.querySelectorAll(".zona-archivo").forEach(zona => {
        const input = zona.querySelector('input[type="file"]');
        const nombre = zona.querySelector(".zona-archivo-nombre");
        const detalle = zona.querySelector(".zona-archivo-detalle");
        const mensajeError = zona.nextElementSibling;
        const textoNombre = nombre.textContent;
        const textoDetalle = detalle.textContent;
        const textoError = mensajeError.textContent;
        const maximoMb = Number(input.dataset.maxMb);

        input.addEventListener("change", () => {
            const archivo = input.files[0];
            let error = "";
            if (archivo && !archivo.name.toLowerCase().endsWith(".pdf")) {
                error = "Solo se permiten archivos PDF.";
            } else if (archivo && archivo.size > maximoMb * BYTES_POR_MB) {
                error = `El archivo pesa ${(archivo.size / BYTES_POR_MB).toFixed(1)} MB; el máximo es ${maximoMb} MB.`;
            }

            input.setCustomValidity(error);
            mensajeError.textContent = error || textoError;
            zona.classList.toggle("con-archivo", Boolean(archivo) && !error);
            nombre.textContent = archivo ? archivo.name : textoNombre;
            detalle.textContent = archivo ? `${(archivo.size / BYTES_POR_MB).toFixed(1)} MB` : textoDetalle;
        });

        ["dragenter", "dragover"].forEach(tipo => zona.addEventListener(tipo, () => zona.classList.add("arrastrando")));
        ["dragleave", "drop"].forEach(tipo => zona.addEventListener(tipo, () => zona.classList.remove("arrastrando")));
    });


    // ==========================================
    // ACTUALIZAR AÑO DEL FOOTER
    // ==========================================

    const footerYear = document.querySelector("footer small");

    if (footerYear) {
        const year = new Date().getFullYear();
        footerYear.textContent = `© ${year} CodeLibrary. Todos los derechos reservados.`;
    }


    // ==========================================
    // ANIMACIÓN AL HACER SCROLL
    // ==========================================

    const elementosAnimados = document.querySelectorAll(".reveal");

    if (!("IntersectionObserver" in window)) {
        elementosAnimados.forEach(elemento => elemento.classList.add("visible"));
        return;
    }

    const observer = new IntersectionObserver(
        (entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add("visible");
                    observer.unobserve(entry.target);
                }
            });
        },
        { threshold: 0.15 }
    );

    elementosAnimados.forEach(elemento => observer.observe(elemento));
});
