# Pruebas, gestión de proyecto e incidentes

## 1. Herramientas de verificación de pruebas
| Herramienta | Uso | Ubicación |
|---|---|---|
| **JUnit 5** | Pruebas unitarias | `src/test/java/.../service` |
| **Mockito** | Simula repositorios/servicios (aislamiento) | mismas clases |
| **Selenium** | Pruebas de interfaz (login, rutas protegidas) | `e2e/LoginSeleniumIT.java` |
| **H2** (perfil `test`) | BD en memoria para prueba de humo y CI | `application-test.properties` |

```bash
./mvnw clean verify -Dspring.profiles.active=test                         # unitarias + humo
./mvnw test -Dtest=LoginSeleniumIT -Dselenium=true -Dbase.url=http://localhost:8081   # UI (app levantada, Chrome instalado)
```

## 2. Plan de casos de prueba
| ID | Caso | Tipo | RF | Resultado esperado |
|---|---|---|---|---|
| CP-01 | Registrar usuario válido | Unitaria | RF-01/03 | Contraseña cifrada, plan GRATUITO, rol LECTOR |
| CP-02 | Registro con DNI duplicado | Unitaria | RF-02 | Error y no se guarda |
| CP-03 | Registro con correo duplicado | Unitaria | RF-02 | Error y no se guarda |
| CP-04 | Intentar registrarse como ADMINISTRADOR | Unitaria | RF-03 | Queda como LECTOR |
| CP-05 | Libro nuevo | Unitaria | RF-07 | Estado PENDIENTE |
| CP-06 | Libro sin título / sin categoría | Unitaria | RF-06 | `IllegalArgumentException` |
| CP-07 | Aprobar libro | Unitaria | RF-09 | Estado APROBADO |
| CP-08 | Cuenta gratuita lee libro Premium | Unitaria | RF-14 | No permitido |
| CP-09 | Cuenta Premium lee libro Premium | Unitaria | RF-14 | Permitido |
| CP-10 | Libro pendiente en vista de lector | Unitaria | RF-11 | 404 |
| CP-11 | Agregar libro repetido a la lista | Unitaria | RF-12 | No duplica |
| CP-12 | Activar Premium | Unitaria | RF-15 | Nivel PREMIUM |
| CP-13..17 | PDF válido, extensión errónea, firma falsa, vacío, *path traversal* | Unitaria | RF-17 | Aceptado/rechazado según el caso |
| CP-18 | La aplicación arranca | Humo | — | Contexto cargado con H2 |
| CP-UI-01..04 | Formulario de login, login inválido, `/admin` sin sesión, catálogo público | UI (Selenium) | RF-04/05/16 | Ver `LoginSeleniumIT` |

Total: **25 pruebas automatizadas** (20 unitarias con Mockito, 1 de humo y 4 de UI con Selenium).

## 3. Herramientas de gestión de proyectos
- **Trello** (recomendado para el curso): tablero con columnas *Backlog · En progreso · En revisión · Hecho*; una tarjeta por paquete de la WBS, con responsable y fecha.
- **Jira**: proyecto Scrum "CLB"; épicas = módulos (Seguridad, Escritor, Admin, Lector, DevOps); sprints semanales.
- **Lucidchart / Google Drive**: diagramas BPMN y repositorio de documentos compartidos.
- **Asana**: alternativa para asignar tareas y plazos.

## 4. Sistema de seguimiento de incidentes (Jira)
Tipo de ticket `Bug`/`Incident` con los campos: título, descripción, pasos para reproducir, severidad, responsable, estado (`Abierto → En análisis → En corrección → Verificado → Cerrado`), versión afectada y *commit* de la corrección.

**Plantilla de registro:**

| ID | Fecha | Descripción | Severidad | Detectado en | Responsable | Estado | Solución |
|---|---|---|---|---|---|---|---|
| INC-001 | [fecha] | [ej. Pipeline falla por no encontrar MySQL] | Alta | CI | [nombre] | Cerrado | Perfil `test` con H2 |
| INC-002 | [fecha] | [ej. Contraseña del admin no definida → no se crea el administrador] | Media | Despliegue | [nombre] | Cerrado | Variable `ADMIN_PASSWORD` en Secret |

> Completa la tabla solo con incidentes que **realmente** ocurrieron en el proyecto; los dos ejemplos son una guía de formato.

## 5. Control de calidad y seguridad (resumen)
CSRF está desactivado temporalmente en `SecurityConfig` (comentado en el código): documentarlo como **riesgo conocido** y proponer activarlo antes de producción.

**Credenciales de la base de datos:** `application.properties` las lee de `DB_USER` / `DB_PASSWORD` (sin contraseña escrita en el código) y `docker-compose.yml` exige `DB_PASSWORD`; en Kubernetes vienen del `Secret`. Esto cumple el RNF-04.
