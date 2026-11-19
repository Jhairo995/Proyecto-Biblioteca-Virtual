# Project Charter básico — CodeLibrary (Biblioteca Virtual)

| Campo | Detalle |
|---|---|
| **Proyecto** | CodeLibrary – Biblioteca Virtual de libros técnicos en PDF |
| **Curso** | Herramientas de Desarrollo (UTP) — Avance de Proyecto Final 3 |
| **Docente** | Anaximando Fernandez Guerrero |
| **Líder del proyecto** | [Completar: integrante designado] |
| **Equipo** | Adrianzen La Rosa Milton Rodrigo · Cortez Sandoval Juan David · Gonzales Escalante Jhair Itiel · Orellano Sánchez Gabriel · Taboada Burgos William Miguel |
| **Fecha de inicio / cierre** | 01/09/2026 – 30/11/2026 (ver Gantt en `02-wbs-gantt.md`) |

## 1. Justificación (problema)
Los lectores técnicos no cuentan con un sistema que reúna libros de programación en un catálogo ordenado, controle quién puede publicarlos (revisión previa) y diferencie contenido gratuito de contenido Premium. La empresa pierde control sobre la calidad del catálogo y no puede generar ingresos por suscripción.

## 2. Objetivo general
Desarrollar y desplegar una aplicación web que permita publicar, revisar, consultar y leer libros PDF, con roles diferenciados (Lector, Escritor, Administrador) y plan Gratuito/Premium.

## 3. Objetivos específicos (medibles)
1. Implementar registro/login con tres roles y contraseñas cifradas (BCrypt) antes del 30/10.
2. Implementar el flujo de publicación: escritor sube PDF → administrador aprueba/rechaza → lector lo ve en el catálogo.
3. Implementar acceso por nivel de cuenta (libros Premium solo para cuentas Premium).
4. Automatizar compilación, pruebas y construcción de imagen Docker con CI (≥ 1 pipeline funcional).
5. Desplegar la solución con contenedores (Docker Compose y manifiestos de Kubernetes).
6. Alcanzar ≥ 20 pruebas automatizadas (JUnit/Mockito) pasando en CI.

## 4. Alcance
**Incluye:** autenticación y roles, catálogo y categorías, lista de lectura personal, visor de PDF, panel del escritor, panel de moderación del administrador, suscripción Premium simulada, CI/CD y despliegue en contenedores.
**Excluye:** pasarela de pago real, aplicación móvil nativa, lector EPUB, sincronización entre dispositivos.

## 5. Entregables principales
Código fuente (GitHub) · Informe APA/IEEE · SRS · Diagramas (BPMN, clases, ER) · Prototipo UX/UI · Pipelines (Jenkins, GitHub Actions, Azure) · Manifiestos Docker/Kubernetes · Plan de pruebas · Presentación PPT.

## 6. Supuestos y restricciones
- Proyecto académico: sin pagos reales; la suscripción Premium se simula.
- Java 25 + Spring Boot; ≥ 90 % del código de programación en Java (JavaScript solo para validaciones en formularios).
- Entrega y sustentación en la semana 15 (≈ 10 minutos por grupo).

## 7. Riesgos principales
| Riesgo | Prob. | Impacto | Mitigación |
|---|---|---|---|
| Subida de archivos maliciosos | Media | Alto | Validar extensión y firma `%PDF`, nombre aleatorio (implementado) |
| Fallo del pipeline por dependencia de BD | Alta | Medio | Perfil `test` con H2 en memoria |
| Contenido sin derechos de distribución | Media | Alto | Revisión obligatoria del administrador antes de publicar |
| Tiempo insuficiente del equipo | Media | Medio | Seguimiento semanal en Trello/Jira; alcance acotado |

## 8. Presupuesto y recursos (referencial)
Recursos humanos: 5 estudiantes. Infraestructura: GitHub (gratis), Docker, instancia cloud de prueba (nivel gratuito). Costo estimado en dinero: S/ 0 (capas gratuitas).

## 9. Aprobación
| Rol | Nombre | Firma / Fecha |
|---|---|---|
| Docente | Anaximando Fernandez Guerrero | |
| Líder del proyecto | [Completar] | |
