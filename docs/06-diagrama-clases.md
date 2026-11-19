# Diagrama de clases (según el código fuente)

```mermaid
classDiagram
    direction LR
    class Usuario {
        -Long id
        -String nombre
        -String correo
        -String contrasena
        -Rol rol
        -NivelCuenta nivelCuenta
        -String dni
    }
    class Libro {
        -Long id
        -String titulo
        -String descripcion
        -String categoria
        -String idioma
        -NivelCuenta nivelAcceso
        -EstadoLibro estado
        -String archivo
        -LocalDateTime fechaRegistro
        +getPortada() String
    }
    class Lectura {
        -Long id
        -LocalDateTime fechaAgregado
    }
    class Rol { <<enumeration>> LECTOR ESCRITOR ADMINISTRADOR }
    class NivelCuenta { <<enumeration>> GRATUITO PREMIUM }
    class EstadoLibro { <<enumeration>> PENDIENTE APROBADO RECHAZADO }

    class UsuarioService { +registrarUsuario(Usuario) Usuario }
    class LibroService {
        +registrarLibro(DatosLibro, MultipartFile, Usuario) Libro
        +cambiarEstado(Long, EstadoLibro) Libro
        +librosPorEstado(EstadoLibro) List
    }
    class LectorService {
        +catalogo() List
        +puedeLeer(Usuario, Libro) boolean
        +agregarALista(Usuario, Long) Libro
        +activarPremium(Usuario) void
    }
    class AlmacenamientoService {
        +guardarPdf(MultipartFile) String
        +cargar(String) Resource
    }

    class AuthController
    class LectorController
    class EscritorController
    class AdminController
    class SecurityConfig
    class UsuarioDetailsService

    Usuario "1" --> "*" Libro : escritor
    Usuario "1" --> "*" Lectura : lector
    Libro "1" --> "*" Lectura
    Usuario --> Rol
    Usuario --> NivelCuenta
    Libro --> EstadoLibro
    Libro --> NivelCuenta

    AuthController --> UsuarioService
    LectorController --> LectorService
    LectorController --> LibroService
    EscritorController --> LibroService
    AdminController --> LibroService
    LibroService --> AlmacenamientoService
    SecurityConfig ..> UsuarioDetailsService
```

## Patrones y buenas prácticas aplicadas
- **Arquitectura en capas MVC**: Controller → Service → Repository.
- **Inyección de dependencias por constructor** (facilita las pruebas con Mockito).
- **Repository (Spring Data JPA)** para el acceso a datos.
- **Enumeraciones** para roles, planes y estados (evita "strings mágicos").
- **Record** `DatosLibro` como objeto de transferencia de datos del formulario.
