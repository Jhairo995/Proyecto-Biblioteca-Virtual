# Proyecto Biblioteca Virtual — CodeLibrary

Repositorio del **Proyecto Biblioteca Virtual (CodeLibrary)**, una aplicación web desarrollada con **Spring Boot**, **Thymeleaf** y **MySQL/MariaDB** para publicar, revisar, consultar y leer **libros técnicos en PDF**, con tres roles (Lector, Escritor, Administrador) y plan Gratuito/Premium.

---

## Integrantes del Equipo


| N° | Nombres y Apellidos | Código / Matrícula |
|:--:|:--------------------|:-------------------|
| 1  | [Adrianzen La Rosa Milton Rodrigo] | [U23200543] |
| 2  | [Orellano Sánchez Gabriel] | [U23205074] |
| 3  | [Taboada Burgos William Miguel] | [U23214227] |
| 4  | [Gonzales Escalante Jhair Itiel] | [U23270363] |
| 5  | [Cortez Sandoval Juan David] | [U23215010] |

---

## Características principales

- **Registro e inicio de sesión** con tres roles: **Lector**, **Escritor** y **Administrador** (el administrador no puede registrarse públicamente; se crea al arrancar con `ADMIN_PASSWORD`). Contraseñas cifradas con BCrypt.
- **Publicación de libros (Escritor):** sube un PDF (máx. 20 MB) con título, descripción, categoría, idioma y nivel de acceso. Todo libro nuevo queda **PENDIENTE**.
- **Moderación (Administrador):** revisa el PDF y **aprueba o rechaza** cada libro; ve indicadores de usuarios y libros.
- **Catálogo y lectura (Lector):** solo ve libros **APROBADOS**, los lee en un visor de PDF integrado y arma su **lista de lectura** personal.
- **Plan Gratuito / Premium:** los libros Premium solo se leen con cuenta Premium. La suscripción es **simulada** (no hay pasarela de pago real).
- **Páginas públicas:** inicio, catálogo, categorías y suscripción.

---

## Tecnologías utilizadas

- **Lenguaje:** Java 25
- **Framework:** Spring Boot 4.1 (Spring MVC, Spring Security, Spring Data JPA, Spring Validation)
- **Frontend:** Thymeleaf + HTML5 / CSS3 / Bootstrap 5 / JavaScript (solo validaciones de formularios)
- **Base de datos:** MySQL / MariaDB (esquema `biblioteca_virtual`); H2 en memoria solo para pruebas
- **Gestor de dependencias:** Apache Maven (wrapper `./mvnw` incluido)
- **Apoyo:** Project Lombok
- **Pruebas:** JUnit 5, Mockito, Selenium
- **DevOps:** Docker, Docker Compose, Kubernetes, Jenkins, GitHub Actions, Azure Pipelines

---

## Requisitos previos

1. **Java JDK 25** (o superior).
2. **Apache Maven 3.8+** (o el wrapper `./mvnw`).
3. **MySQL 8.0+ o MariaDB** en el puerto `3306` (por ejemplo, el de XAMPP).
4. **IDE recomendado:** IntelliJ IDEA, Eclipse o VS Code con soporte para Spring Boot.

---

## Instalación y configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/Jhairo995/Proyecto-Biblioteca-Virtual.git
cd Proyecto-Biblioteca-Virtual
```

### 2. Configurar la base de datos
Crea la base de datos `biblioteca_virtual` (las tablas las genera Hibernate al arrancar):

```sql
CREATE DATABASE IF NOT EXISTS biblioteca_virtual CHARACTER SET utf8mb4;
```

Las credenciales **no están escritas en el código**: `application.properties` las lee de variables de entorno.

| Variable | Para qué sirve | Valor por defecto |
|---|---|---|
| `DB_HOST` | Servidor de la base de datos | `localhost` |
| `DB_PORT` | Puerto de MySQL/MariaDB (en algunos XAMPP es `3307`) | `3306` |
| `DB_USER` | Usuario de la base de datos | `root` |
| `DB_PASSWORD` | Contraseña de la base de datos | vacía (como XAMPP) |
| `ADMIN_PASSWORD` | Contraseña del administrador inicial (`admin@codelibrary.com`) | sin valor: si no se define, **no se crea** el administrador |

```bash
# Linux / macOS
export DB_PASSWORD=tu_clave_de_bd
export ADMIN_PASSWORD=MiClaveAdmin

# Windows (PowerShell)
$env:DB_PASSWORD="tu_clave_de_bd"
$env:ADMIN_PASSWORD="MiClaveAdmin"
```

### 3. Compilar el proyecto
```bash
./mvnw clean compile
```
*(En Windows puedes usar `mvnw.cmd clean compile`)*

### 4. Ejecutar la aplicación
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8081`.

---

## Estructura del proyecto

```text
Proyecto-Biblioteca-Virtual/
├── src/
│   ├── main/
│   │   ├── java/com/example/ejemplodemo/
│   │   │   ├── controller/     # Controladores MVC (auth, lector, escritor, admin)
│   │   │   ├── model/          # Entidades y enumeraciones (Usuario, Libro, Lectura, Rol, ...)
│   │   │   ├── Repository/     # Repositorios Spring Data JPA
│   │   │   ├── Service/        # Lógica de negocio
│   │   │   └── security/       # SecurityConfig, UserDetailsService, administrador inicial
│   │   └── resources/
│   │       ├── templates/      # Vistas Thymeleaf
│   │       ├── static/         # CSS y JS
│   │       └── application.properties
│   └── test/                   # JUnit 5, Mockito y Selenium
├── docs/                       # Documentación técnica (ver abajo)
├── k8s/                        # Manifiestos de Kubernetes
├── Dockerfile · docker-compose.yml · Jenkinsfile · azure-pipelines.yml · .github/workflows/ci.yml
├── pom.xml
└── README.md
```

---

## Licencia

Este proyecto se realiza con fines académicos para el curso de **HERRAMIENTAS DE DESARROLLO**.

---

## DevOps y despliegue (Avance de Proyecto Final 3)

| Necesidad | Archivo |
|---|---|
| Imagen Docker | `Dockerfile`, `.dockerignore` |
| App + BD con un comando | `docker-compose.yml` |
| Jenkins local | `docker-compose.jenkins.yml`, `Jenkinsfile` |
| GitHub Actions | `.github/workflows/ci.yml` |
| Azure Pipelines | `azure-pipelines.yml` |
| Kubernetes | `k8s/` |
| Pruebas | `src/test/java` (JUnit 5, Mockito, Selenium) |

### Ejecutar con Docker
```bash
cp .env.example .env        # edita DB_PASSWORD y ADMIN_PASSWORD (DB_PASSWORD es obligatoria)
docker compose up --build   # app + MariaDB 10.11; agrega -d para dejarlo en segundo plano
```
Abrir `http://localhost:8081` (si el puerto está ocupado, cambia `APP_PORT` en `.env`).
Detener: `docker compose down` (agrega `-v` para borrar también la BD y los PDFs).

### Ejecutar las pruebas
```bash
./mvnw clean verify -Dspring.profiles.active=test
```

### Documentación técnica (`docs/`)
1. [Project Charter](docs/01-project-charter.md)
2. [WBS y Gantt](docs/02-wbs-gantt.md)
3. [SRS (RF y RNF)](docs/03-srs.md)
4. [Procesos BPMN](docs/04-bpm-procesos.md)
5. [Base de datos (ER y DDL)](docs/05-base-de-datos.md)
6. [Diagrama de clases](docs/06-diagrama-clases.md)
7. [UX/UI y reportes](docs/07-ux-ui-reportes.md)
8. [DevOps, nube y Kubernetes](docs/08-devops.md)
9. [Pruebas, gestión e incidentes](docs/09-pruebas-y-gestion.md)
