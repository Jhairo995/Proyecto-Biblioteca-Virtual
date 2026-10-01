```javascript
document.addEventListener("DOMContentLoaded", () => {

    // ==========================================
    // MENÚ NAVBAR
    // ==========================================

    const navLinks = document.querySelectorAll(".navbar-nav .nav-link");

    navLinks.forEach(link => {
        link.addEventListener("click", () => {

            // Quitar clase active de todos los enlaces
            navLinks.forEach(item => item.classList.remove("active"));

            // Activar el enlace seleccionado
            link.classList.add("active");

            // Cerrar el menú en dispositivos móviles
            const menu = document.querySelector("#menuPrincipal");

            if (menu.classList.contains("show")) {
                const navbarToggler = document.querySelector(".navbar-toggler");

                if (navbarToggler) {
                    navbarToggler.click();
                }
            }
        });
    });


    // ==========================================
    // BOTONES "LEER"
    // ==========================================

    const botonesLeer = document.querySelectorAll(".book-card .btn-primary");

    botonesLeer.forEach(boton => {
        boton.addEventListener("click", (event) => {
            event.preventDefault();

            const tarjeta = boton.closest(".book-card");
            const titulo = tarjeta.querySelector(".card-title").textContent.trim();

            alert(`Has seleccionado el libro: "${titulo}"`);
        });
    });


    // ==========================================
    // BOTONES DE SUSCRIPCIÓN
    // ==========================================

    const botonesSuscripcion = document.querySelectorAll(
        "#planes .btn-primary, #planes .btn-outline-primary"
    );

    botonesSuscripcion.forEach(boton => {
        boton.addEventListener("click", (event) => {
            event.preventDefault();

            const tarjeta = boton.closest(".pricing-card");
            const nombrePlan = tarjeta.querySelector("h4").textContent.trim();

            alert(`Has seleccionado el plan ${nombrePlan}.`);
        });
    });


    // ==========================================
    // BOTÓN "COMENZAR AHORA"
    // ==========================================

    const botonComenzar = document.querySelector(
        '.hero a[href="#planes"]'
    );

    if (botonComenzar) {
        botonComenzar.addEventListener("click", () => {
            console.log("El usuario quiere comenzar a aprender.");
        });
    }


    // ==========================================
    // BOTONES "VER RECURSOS"
    // ==========================================

    const botonesRecursos = document.querySelectorAll(
        ".category-card .btn"
    );

    botonesRecursos.forEach(boton => {
        boton.addEventListener("click", (event) => {
            event.preventDefault();

            const tarjeta = boton.closest(".category-card");
            const categoria = tarjeta.querySelector("h5").textContent.trim();

            alert(`Has seleccionado la categoría: ${categoria}`);
        });
    });


    // ==========================================
    // ACTUALIZAR AÑO DEL FOOTER
    // ==========================================

    const footerYear = document.querySelector("footer small");

    if (footerYear) {
        const year = new Date().getFullYear();

        footerYear.textContent =
            `© ${year} CodeLibrary. Todos los derechos reservados.`;
    }


    // ==========================================
    // ANIMACIÓN AL HACER SCROLL
    // ==========================================

    const elementosAnimados = document.querySelectorAll(
        ".category-card, .book-card, .pricing-card"
    );

    const observer = new IntersectionObserver(
        (entries) => {

            entries.forEach(entry => {

                if (entry.isIntersecting) {

                    entry.target.style.opacity = "1";
                    entry.target.style.transform = "translateY(0)";

                    observer.unobserve(entry.target);
                }
            });

        },
        {
            threshold: 0.15
        }
    );


    elementosAnimados.forEach(elemento => {

        elemento.style.opacity = "0";
        elemento.style.transform = "translateY(20px)";
        elemento.style.transition = "opacity 0.6s ease, transform 0.6s ease";

        observer.observe(elemento);
    });


    // ==========================================
    // MENSAJE EN CONSOLA
    // ==========================================

    console.log("CodeLibrary cargado correctamente.");
});
```
