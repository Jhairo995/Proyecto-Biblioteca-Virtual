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

1. **Java JDK 25** (es la versión que exige `pom.xml`; con una versión anterior no compila).
2. **Apache Maven 3.8+** (o utilizar el wrapper `./mvnw` incluido).
3. **MySQL Server 8.0+** o el **MariaDB de XAMPP** corriendo en el puerto por defecto (`3306`).
4. **IDE recomendado:** IntelliJ IDEA, Eclipse o VS Code con soporte para Spring Boot.

---

##  Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/Jhairo995/Proyecto-Biblioteca-Virtual.git
cd Proyecto-Biblioteca-Virtual
```

### 2. Configurar la Base de Datos
No hace falta crear nada a mano: al arrancar se crea la base de datos `biblioteca_virtual` y Hibernate genera las tablas.

El proyecto se conecta como `root` con la contraseña `123456`. Si tu MySQL usa otra (en XAMPP suele estar vacía), no edites `application.properties`: define estas variables de entorno antes de ejecutar:

```bash
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=          # vacía en XAMPP
```

### 3. Crear el administrador
Los administradores no pueden registrarse desde la web. El administrador inicial (`admin@codelibrary.com`) se crea al arrancar **solo si** defines la variable de entorno `ADMIN_PASSWORD`:

```bash
export ADMIN_PASSWORD='elige_una_contraseña'        # Linux / macOS
$env:ADMIN_PASSWORD = 'elige_una_contraseña'        # Windows PowerShell
```

En IntelliJ o VS Code agrégala en las *Environment variables* de la configuración de ejecución. Si no está definida, la aplicación arranca igual pero el log muestra `No se creó el administrador`.

- Solo se crea una vez: cambiar `ADMIN_PASSWORD` después **no** cambia la contraseña del administrador.
- El correo `admin@codelibrary.com` y el DNI `00000000` están reservados: nadie puede registrarse con ellos.

### 4. Compilar el Proyecto
Ejecuta el siguiente comando en la terminal:
```bash
./mvnw clean compile
```
*(En Windows puedes usar `mvnw.cmd clean compile`)*

### 5. Ejecutar la Aplicación
Para iniciar el servidor de desarrollo local:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en tu navegador en:
`http://localhost:8081`

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
