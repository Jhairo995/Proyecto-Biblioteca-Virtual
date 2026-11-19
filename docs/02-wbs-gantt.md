# WBS y Diagrama de Gantt

> Los diagramas están en Mermaid: se visualizan directamente en GitHub. Para el informe, exporta una captura o recréalos en Lucidchart/Trello/Asana.

## WBS (Estructura de Desglose del Trabajo)

```mermaid
mindmap
  root((CodeLibrary))
    1. Gestión
      1.1 Project Charter
      1.2 Plan y Gantt
      1.3 Seguimiento en Trello/Jira
    2. Análisis
      2.1 Contexto y Lean Canvas
      2.2 SRS RF/RNF
      2.3 Procesos BPMN
    3. Diseño
      3.1 Diagrama de clases
      3.2 Modelo ER y BD física
      3.3 Prototipo UX/UI
    4. Desarrollo
      4.1 Seguridad y roles
      4.2 Módulo Escritor
      4.3 Módulo Administrador
      4.4 Módulo Lector y Premium
    5. Calidad
      5.1 Pruebas unitarias JUnit/Mockito
      5.2 Pruebas UI Selenium
      5.3 Registro de incidentes
    6. DevOps
      6.1 Dockerfile y Compose
      6.2 CI Jenkins / GitHub Actions / Azure
      6.3 Despliegue Kubernetes
    7. Cierre
      7.1 Informe APA/IEEE
      7.2 Presentación PPT
      7.3 Sustentación
```

## Diccionario WBS (resumen)
| Código | Paquete de trabajo | Entregable | Responsable |
|---|---|---|---|
| 1.1 | Project Charter | `01-project-charter.md` | [Líder] |
| 2.2 | SRS | `03-srs.md` | [Completar] |
| 2.3 | Procesos BPMN | `04-bpm-procesos.md` | [Completar] |
| 3.1–3.2 | Clases y ER | `05`, `06` | [Completar] |
| 4.x | Desarrollo | Código en `src/main` | [Completar] |
| 5.1 | Pruebas unitarias | `src/test` | [Completar] |
| 6.x | DevOps | Dockerfile, Jenkinsfile, `.github`, `azure-pipelines.yml`, `k8s/` | [Completar] |
| 7.x | Cierre | Informe + PPT | Todos |

## Gantt

```mermaid
gantt
    title Cronograma CodeLibrary (Sep - Nov 2026)
    dateFormat  YYYY-MM-DD
    axisFormat  %d/%m

    section Gestión
    Project Charter               :done, g1, 2026-09-01, 7d
    Aprobación del problema       :done, g2, 2026-09-08, 5d
    Seguimiento semanal           :active, g3, 2026-09-15, 75d

    section Análisis
    Contexto y Lean Canvas        :done, a1, 2026-09-15, 10d
    SRS (RF y RNF)                :done, a2, 2026-09-25, 10d
    Procesos BPMN                 :active, a3, 2026-10-20, 10d

    section Diseño
    Diagrama de clases y ER       :done, d1, 2026-10-05, 10d
    Prototipo UX/UI               :done, d2, 2026-10-10, 12d

    section Desarrollo
    Seguridad y roles             :done, v1, 2026-10-12, 10d
    Módulo Escritor y Admin       :done, v2, 2026-10-22, 12d
    Módulo Lector y Premium       :done, v3, 2026-11-01, 8d

    section Calidad
    Pruebas JUnit y Mockito       :active, q1, 2026-11-10, 6d
    Pruebas Selenium              :q2, 2026-11-14, 4d

    section DevOps
    Docker y Compose              :active, o1, 2026-11-12, 4d
    CI (Jenkins/GitHub/Azure)     :o2, 2026-11-14, 5d
    Despliegue Kubernetes         :o3, 2026-11-18, 5d

    section Cierre
    Informe y PPT                 :c1, 2026-11-20, 7d
    Sustentación (semana 15)      :milestone, c2, 2026-11-28, 1d
```
