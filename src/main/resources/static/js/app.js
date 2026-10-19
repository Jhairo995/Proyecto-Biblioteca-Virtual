document.addEventListener("DOMContentLoaded", () => {

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

            alert(`Has seleccionado el libro: "${titulo}"`);
        });
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
