#  Proyecto Biblioteca Virtual

Bienvenido al repositorio del **Proyecto Biblioteca Virtual**, una aplicación web desarrollada con **Spring Boot**, **Thymeleaf** y **MySQL** para la gestión y préstamo digital de libros y recursos educativos.

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

##  Características Principal

-  **Catálogo de Libros:** Consulta y búsqueda de títulos, autores y categorías.
-  **Gestión de Usuarios y Membresías:** Control de acceso, tipos de usuario (Básico, Premium, VIP) y estado de matrícula.
-  **Préstamos y Reservas:** Sistema de reserva de libros en línea y control de préstamos activos.
-  **Panel Administrativo:** Gestión de inventario de biblioteca y reportes.

---

##  Tecnologías Utilizadas

- **Lenguaje de Programación:** Java (JDK 17+)
- **Framework Principal:** [Spring Boot 3](https://spring.io/projects/spring-boot)
  - Spring MVC
  - Spring Validation
- **Motor de Plantillas (Frontend):** [Thymeleaf](https://www.thymeleaf.org/) + HTML5 / CSS3 / JavaScript
- **Base de Datos:** MySQL
- **Gestor de Dependencias:** Apache Maven
- **Herramientas de Apoyo:** Project Lombok

---

##  Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de contar con lo siguiente instalado en tu equipo:

1. **Java JDK 17** o superior.
2. **Apache Maven 3.8+** (o utilizar el wrapper `./mvnw` incluido).
3. **MySQL Server 8.0+** corriendo en el puerto por defecto (`3306`).
4. **IDE recomendado:** IntelliJ IDEA, Eclipse o VS Code con soporte para Spring Boot.

---

##  Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/Jhairo995/Proyecto-Biblioteca-Virtual.git
cd Proyecto-Biblioteca-Virtual
```

### 2. Configurar la Base de Datos
Crea una base de datos MySQL llamada `bd_biblioteca` (o edita la configuración en `src/main/resources/application.properties` con tus credenciales de MySQL):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bd_biblioteca?useSSL=false&serverTimezone=UTC
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

### 3. Compilar el Proyecto
Ejecuta el siguiente comando en la terminal:
```bash
./mvnw clean compile
```
*(En Windows puedes usar `mvnw.cmd clean compile`)*

### 4. Ejecutar la Aplicación
Para iniciar el servidor de desarrollo local:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en tu navegador en:
`http://localhost:8080`

---

##  Estructura del Proyecto

```text
Proyecto-Biblioteca-Virtual/
├── src/
│   ├── main/
│   │   ├── java/com/example/ejemplodemo/
│   │   │   ├── controller/     # Controladores MVC de Spring
│   │   │   ├── model/          # Entidades y Modelos de datos
│   │   │   ├── repository/     # Repositorios JPA / Acceso a datos
│   │   │   └── service/        # Lógica de negocio
│   │   └── resources/
│   │       ├── templates/      # Vistas HTML con Thymeleaf
│   │       ├── static/         # Archivos estáticos (CSS, JS, Imágenes)
│   │       └── application.properties # Configuración de Spring Boot
├── pom.xml                     # Configuración y dependencias Maven
└── README.md                   # Documentación del proyecto
```

---

##  Licencia

Este proyecto se realiza con fines académicos para el curso de **HERRAMIENTAS DE DESARROLLO**.
