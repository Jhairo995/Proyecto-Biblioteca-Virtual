# Modelado de procesos de negocio (BPM / BPMN 2.0)

**Notación usada.** Evento de inicio (círculo), actividad (rectángulo redondeado), compuerta exclusiva XOR (rombo con ✕), evento de fin (círculo grueso), *pools/lanes* por participante. Los diagramas siguientes usan Mermaid como aproximación visual; para la entrega final se recomienda redibujarlos en **Bizagi Modeler, Camunda Modeler o Lucidchart** con notación BPMN formal.

## Proceso 1: Publicación y moderación de un libro (TO-BE)

```mermaid
flowchart TD
    subgraph Escritor
      A((Inicio)) --> B[Completar formulario y adjuntar PDF]
      B --> C[Enviar libro]
    end
    subgraph Sistema
      C --> D{PDF válido?<br/>extensión + firma}
      D -- No --> E[Mostrar error] --> B
      D -- Sí --> F[Guardar archivo con nombre UUID]
      F --> G[Registrar libro: estado PENDIENTE]
    end
    subgraph Administrador
      G --> H[Revisar PDF y datos]
      H --> I{Cumple las reglas?}
      I -- Sí --> J[Aprobar]
      I -- No --> K[Rechazar]
    end
    J --> L[Libro visible en catálogo]
    K --> M[Libro oculto; el escritor ve estado RECHAZADO]
    L --> Z((Fin))
    M --> Z
```

## Proceso 2: Lectura de un libro con control de plan

```mermaid
flowchart TD
    A((Inicio)) --> B[Lector inicia sesión]
    B --> C[Explora catálogo de libros aprobados]
    C --> D[Selecciona un libro]
    D --> E{Libro Premium?}
    E -- No --> G[Mostrar visor PDF]
    E -- Sí --> F{Cuenta Premium?}
    F -- Sí --> G
    F -- No --> H[Redirigir a Suscripción]
    H --> I{Desea activar Premium?}
    I -- Sí --> J[Activar Premium simulado] --> G
    I -- No --> Z
    G --> K[Opcional: agregar a mi lista]
    K --> Z((Fin))
```

## Proceso 3: Registro de usuario

```mermaid
flowchart TD
    A((Inicio)) --> B[Completar nombre, correo, DNI, contraseña, rol]
    B --> C{DNI o correo ya existen?}
    C -- Sí --> D[Mostrar error] --> B
    C -- No --> E[Cifrar contraseña BCrypt]
    E --> F[Crear cuenta con plan GRATUITO]
    F --> G((Fin))
```

## Explicación de elementos BPMN usados
| Elemento | Ejemplo en el modelo | Significado |
|---|---|---|
| Evento de inicio / fin | Inicio, Fin | Dónde comienza y termina el proceso |
| Tarea | "Revisar PDF y datos" | Trabajo realizado por un participante |
| Compuerta exclusiva (XOR) | "¿Libro Premium?" | Solo un camino según una condición |
| Carril (lane) | Escritor / Sistema / Administrador | Responsable de cada actividad |
| Flujo de secuencia | Flechas | Orden de ejecución |
