# DevOps: nube, contenedores, integración continua y despliegue

## 1. Plataformas en la nube
La aplicación es *cloud-agnostic* porque se entrega como imagen Docker. Opciones evaluadas:

| Plataforma | Servicio recomendado | Uso en el proyecto |
|---|---|---|
| Microsoft Azure | Azure Container Registry + AKS / Container Apps + Azure Database for MySQL | Destino del `azure-pipelines.yml` |
| AWS | ECR + ECS/EKS + RDS | Alternativa |
| Render / Railway / Fly.io | Despliegue directo de la imagen | Opción gratuita para la demo |

Modelos: la BD gestionada es **PaaS**; los contenedores sobre VM/Kubernetes son **IaaS/CaaS**.

## 2. Contenedores
- `Dockerfile` multi-etapa: compila con `eclipse-temurin:25-jdk` y ejecuta con `eclipse-temurin:25-jre` (imagen liviana, usuario sin privilegios).
- `docker-compose.yml`: orquesta `app` + `db` (MariaDB) con volúmenes persistentes para la BD y los PDF.

```bash
# Ejecutar todo localmente
ADMIN_PASSWORD=MiClaveAdmin DB_PASSWORD=MiClaveBD docker compose up --build
# Abrir http://localhost:8081  (admin@codelibrary.com / MiClaveAdmin)
```

## 3. Despliegue con aplicación de ejecución de contenedores (Kubernetes)
Manifiestos en `k8s/`: `namespace.yaml`, `secret.example.yaml`, `mariadb.yaml`, `app-deployment.yaml`, `app-service.yaml`.

```bash
# Minikube / cluster de prueba
kubectl apply -f k8s/namespace.yaml
cp k8s/secret.example.yaml k8s/secret.yaml   # editar claves
kubectl apply -f k8s/secret.yaml -f k8s/mariadb.yaml
sed "s|IMAGEN_APP|tu-usuario/biblioteca-virtual:latest|" k8s/app-deployment.yaml | kubectl apply -f -
kubectl apply -f k8s/app-service.yaml
kubectl -n biblioteca get pods,svc
minikube service biblioteca-app -n biblioteca      # abre la app en el navegador
```
Decisiones: sondas *readiness/liveness* en `/login`; secretos en `Secret` de Kubernetes; PVC para BD y PDF. **Limitación:** 1 réplica porque los PDF están en un volumen local; para escalar se debe pasar el almacenamiento a un servicio de objetos (S3/Azure Blob).

## 4. Integración continua (CI/CD)
| Herramienta | Archivo | Qué hace |
|---|---|---|
| **GitHub Actions** | `.github/workflows/ci.yml` | En cada push/PR: compila, ejecuta pruebas, sube reportes y `.jar`; en `main` publica imagen a GHCR |
| **Jenkins** | `Jenkinsfile` (+ `docker-compose.jenkins.yml`) | Compila y prueba en contenedor JDK 25, publica resultados JUnit, construye/publica imagen y despliega en Kubernetes |
| **Azure Pipelines** | `azure-pipelines.yml` | Build + pruebas → imagen en ACR → despliegue en AKS con aprobación |
| **Git** | Ramas `main`, `develop`, `feature/*`, `fix/*` | Control de versiones y revisión por Pull Request |

Flujo: `feature/*` → Pull Request → CI (compilar + pruebas) → revisión → merge a `develop`/`main` → imagen Docker → despliegue.

Para levantar Jenkins local: `docker compose -f docker-compose.jenkins.yml up -d` y abrir `http://localhost:8080`.

## 5. Evidencias a capturar para el informe
1. Ejecución verde del workflow en la pestaña *Actions* de GitHub.
2. Salida de `docker compose up` y la app en `localhost:8081`.
3. Stage view de Jenkins con las 4 etapas.
4. `kubectl get pods -n biblioteca` con los pods en `Running`.
5. Reporte de pruebas (`./mvnw test`) y tablero Trello/Jira.
