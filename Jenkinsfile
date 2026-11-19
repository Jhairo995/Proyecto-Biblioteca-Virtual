// Pipeline declarativo de Jenkins — Biblioteca Virtual
// Requisitos: plugins "Pipeline", "Docker Pipeline" y "JUnit"; credenciales:
//   - dockerhub-credentials (usuario/contraseña de Docker Hub)  [solo para publicar la imagen]
//   - kubeconfig-biblioteca (archivo kubeconfig)                  [solo para desplegar en Kubernetes]
pipeline {
    agent none

    environment {
        IMAGE_NAME = 'tu-usuario-dockerhub/biblioteca-virtual'   // <-- cambiar
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {
        stage('Compilar y probar') {
            agent {
                docker {
                    image 'eclipse-temurin:25-jdk'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh 'chmod +x mvnw'
                // El perfil "test" usa H2: no necesita MySQL en el servidor de CI
                sh './mvnw -B clean verify -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Construir imagen Docker') {
            agent any
            steps {
                sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -t $IMAGE_NAME:latest .'
            }
        }

        stage('Publicar imagen') {
            agent any
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                    sh 'echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin'
                    sh 'docker push $IMAGE_NAME:$IMAGE_TAG && docker push $IMAGE_NAME:latest'
                }
            }
        }

        stage('Desplegar en Kubernetes') {
            agent any
            when { branch 'main' }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-biblioteca', variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl apply -f k8s/namespace.yaml
                        kubectl apply -f k8s/mariadb.yaml -f k8s/app-service.yaml
                        sed "s|IMAGEN_APP|$IMAGE_NAME:$IMAGE_TAG|" k8s/app-deployment.yaml | kubectl apply -f -
                        kubectl -n biblioteca rollout status deployment/biblioteca-app --timeout=180s
                    '''
                }
            }
        }
    }

    post {
        failure { echo 'El pipeline falló: revisar la consola y registrar el incidente en Jira.' }
    }
}
