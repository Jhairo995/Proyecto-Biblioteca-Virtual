# Especificación de Requerimientos de Software (SRS) — IEEE 830

## 1. Introducción
**Propósito.** Definir los requerimientos de CodeLibrary, una biblioteca virtual web de libros técnicos en PDF.
**Alcance.** Ver `01-project-charter.md`. **Usuarios:** Lector, Escritor, Administrador.
**Definiciones.** *Libro Premium*: solo accesible para cuentas Premium. *Estado de libro*: PENDIENTE, APROBADO, RECHAZADO.

## 2. Descripción general
- **Perspectiva:** aplicación web monolítica (Spring Boot + Thymeleaf) con base de datos relacional.
- **Restricciones:** Java ≥ 90 %, MySQL/MariaDB, ejecución en contenedores.
- **Supuestos:** el administrador inicial se crea con la variable `ADMIN_PASSWORD`.

## 3. Requerimientos funcionales
| ID | Requerimiento | Prioridad | Rol |
|---|---|---|---|
| RF-01 | El sistema permitirá registrarse con nombre, correo, DNI (8 dígitos) y contraseña, eligiendo rol Lector o Escritor. | Alta | Público |
| RF-02 | El sistema rechazará registros con correo o DNI ya existentes. | Alta | Público |
| RF-03 | Toda cuenta nueva iniciará con plan Gratuito; no se permite registrarse como Administrador. | Alta | Sistema |
| RF-04 | El sistema permitirá iniciar y cerrar sesión con correo y contraseña. | Alta | Todos |
| RF-05 | Tras el login se redirigirá al panel según el rol (`/lector`, `/escritor`, `/admin`). | Alta | Todos |
| RF-06 | El Escritor podrá publicar un libro (título, descripción, categoría, idioma, nivel de acceso, PDF). | Alta | Escritor |
| RF-07 | Todo libro nuevo quedará en estado PENDIENTE. | Alta | Sistema |
| RF-08 | El Escritor podrá ver el estado de sus libros y abrir su PDF. | Media | Escritor |
| RF-09 | El Administrador podrá revisar el PDF y aprobar o rechazar libros pendientes. | Alta | Administrador |
| RF-10 | El Administrador verá indicadores: usuarios, libros aprobados y pendientes. | Media | Administrador |
| RF-11 | Solo los libros APROBADOS serán visibles para el Lector. | Alta | Lector |
| RF-12 | El Lector podrá agregar y quitar libros de su lista de lectura (sin duplicados). | Media | Lector |
| RF-13 | El Lector podrá leer el PDF en el visor integrado. | Alta | Lector |
| RF-14 | Los libros Premium solo podrán leerse con cuenta Premium; si no, se redirige a Suscripción. | Alta | Lector |
| RF-15 | El Lector podrá activar el plan Premium (simulado, sin pago real). | Media | Lector |
| RF-16 | Existirán páginas públicas: inicio, catálogo, categorías y suscripción. | Media | Público |
| RF-17 | El sistema aceptará únicamente archivos PDF válidos (extensión y firma `%PDF`), máx. 20 MB. | Alta | Sistema |

## 4. Requerimientos no funcionales
| ID | Categoría | Requerimiento |
|---|---|---|
| RNF-01 | Seguridad | Contraseñas cifradas con BCrypt; nunca se almacenan en texto plano. |
| RNF-02 | Seguridad | Control de acceso por rol en servidor (`/admin`, `/escritor`, `/lector`). |
| RNF-03 | Seguridad | Archivos guardados con nombre aleatorio (UUID) y protección contra *path traversal*. |
| RNF-04 | Seguridad | Credenciales y contraseña del administrador por variables de entorno/Secrets, no en el código. |
| RNF-05 | Rendimiento | Las páginas del catálogo deben responder en < 2 s con 1 000 libros (carga normal). |
| RNF-06 | Usabilidad | Interfaz responsiva (Bootstrap) para escritorio y móvil. |
| RNF-07 | Portabilidad | Empaquetada en imagen Docker; ejecutable con `docker compose up`. |
| RNF-08 | Mantenibilidad | Arquitectura en capas (controller/service/repository); ≥ 20 pruebas automatizadas. |
| RNF-09 | Disponibilidad | Sondas *readiness/liveness* en Kubernetes sobre `/login`. |
| RNF-10 | Integración | Cada *push* ejecuta compilación y pruebas en CI. |

## 5. Casos de uso principales
| Caso | Actor | Resumen |
|---|---|---|
| CU-01 Registrarse | Visitante | Completa el formulario; el sistema valida y crea la cuenta. |
| CU-02 Publicar libro | Escritor | Sube PDF y datos; queda PENDIENTE. |
| CU-03 Moderar libro | Administrador | Revisa PDF; aprueba o rechaza. |
| CU-04 Leer libro | Lector | Elige un libro aprobado; el sistema valida su nivel y muestra el PDF. |
| CU-05 Activar Premium | Lector | Confirma suscripción; cambia su nivel de cuenta. |

## 6. Trazabilidad (resumen)
| Requerimiento | Código / prueba |
|---|---|
| RF-01..03 | `UsuarioService` · `UsuarioServiceTest` |
| RF-06/07 | `LibroService.registrarLibro` · `LibroServiceTest` |
| RF-09 | `LibroService.cambiarEstado` · `LibroServiceTest` |
| RF-11/12/14/15 | `LectorService` · `LectorServiceTest` |
| RF-17, RNF-03 | `AlmacenamientoService` · `AlmacenamientoServiceTest` |
| RF-04/05, RNF-02 | `SecurityConfig` · `LoginSeleniumIT` |
