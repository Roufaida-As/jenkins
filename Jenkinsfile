pipeline {
    agent any

    environment {
        // Variables d'environnement
        MAVEN_REPO_URL = "${env.MAVEN_REPO_URL}"
        SONAR_HOST_URL = "${env.SONAR_HOST_URL}"
        PROJECT_NAME = "TP7-API-INTEGRATION"
        PROJECT_VERSION = "1.0-SNAPSHOT"
    }

    stages {
        // ============================================
        // PHASE 1: TEST
        // ============================================
        stage('Test') {
            steps {
                echo '========== Phase Test =========='

                // Étape 1.1: Lancement des tests unitaires
                echo 'Execution des tests unitaires...'
                bat './gradlew clean test'

                // Étape 1.2: Archivage des résultats des tests
                echo 'Archivage des resultats de tests...'
                junit '**/build/test-results/test/*.xml'

                // Étape 1.3: Génération des rapports Cucumber
                echo 'Generation des rapports Cucumber...'
                bat './gradlew generateCucumberReports'
                cucumber buildStatus: 'UNSTABLE',
                    reportTitle: 'Rapport Cucumber',
                    fileIncludePattern: '**/*.json',
                    jsonReportDirectory: 'reports'
            }
        }

        // ============================================
        // PHASE 2: CODE ANALYSIS (SonarQube)
        // ============================================
        stage('Code Analysis') {
            steps {
                echo '========== Phase Code Analysis =========='
                echo 'Analyse du code avec SonarQube...'

                withSonarQubeEnv('SonarQube') {
                    bat './gradlew sonar'
                }
            }
        }

        // ============================================
        // PHASE 3: CODE QUALITY (Quality Gate)
        // ============================================
        stage('Code Quality') {
            steps {
                echo '========== Phase Code Quality =========='
                echo 'Verification du Quality Gate...'

                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ============================================
        // PHASE 4: BUILD
        // ============================================
        stage('Build') {
            steps {
                echo '========== Phase Build =========='

                // Étape 4.1: Génération du fichier JAR
                echo 'Generation du fichier JAR...'
                bat './gradlew build -x test'

                // Étape 4.2: Génération de la documentation
                echo 'Generation de la Javadoc...'
                bat './gradlew generateJavadoc'

                // Étape 4.3: Archivage du JAR et de la documentation
                echo 'Archivage des artefacts...'
                archiveArtifacts artifacts: '**/build/libs/*.jar',
                    fingerprint: true,
                    allowEmptyArchive: false

                archiveArtifacts artifacts: '**/build/docs/javadoc/**/*',
                    fingerprint: true,
                    allowEmptyArchive: true

                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'build/docs/javadoc',
                    reportFiles: 'index.html',
                    reportName: 'Javadoc',
                    reportTitles: 'Documentation Javadoc'
                ])
            }
        }

        // ============================================
        // PHASE 5: DEPLOY (MyMavenRepo)
        // ============================================
        stage('Deploy') {
            steps {
                echo '========== Phase Deploy =========='
                echo 'Deploiement sur MyMavenRepo...'

                withCredentials([usernamePassword(
                    credentialsId: 'maven-repo-credentials',
                    usernameVariable: 'MAVEN_USERNAME',
                    passwordVariable: 'MAVEN_PASSWORD'
                )]) {
                    bat './gradlew publish'
                }

                echo "Déploiement réussi sur ${MAVEN_REPO_URL}"
            }
        }

        // ============================================
        // PHASE 6: NOTIFICATION (Success)
        // ============================================
        stage('Notification') {
            steps {
                echo '========== Phase Notification =========='

                // Notification par Email
                script {
                    emailext (
                        to: 'asbarroufaida@gmail.com.dz',
                        subject: "Déploiement réussi - ${PROJECT_NAME} v${PROJECT_VERSION}",
                        body: """
                        <html>
                        <body>
                            <h2 style="color: green;">Déploiement réussi</h2>
                            <p>Bonjour,</p>
                            <p>Le déploiement de la librairie a été effectué avec succès par <strong>ASBAR ROUFAIDA</strong>.</p>

                            <h3>Détails du déploiement:</h3>
                            <ul>
                                <li><strong>Projet:</strong> ${PROJECT_NAME}</li>
                                <li><strong>Version:</strong> ${PROJECT_VERSION}</li>
                                <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
                                <li><strong>Date:</strong> ${new Date().format('dd/MM/yyyy HH:mm:ss')}</li>
                                <li><strong>Branch:</strong> ${env.BRANCH_NAME}</li>
                            </ul>

                            <h3>Repository Maven:</h3>
                            <p><a href="${MAVEN_REPO_URL}">${MAVEN_REPO_URL}</a></p>

                            <h3>Utilisation:</h3>
                            <pre>implementation "org.example:${PROJECT_NAME}:${PROJECT_VERSION}"</pre>

                            <h3>Liens utiles:</h3>
                            <ul>
                                <li><a href="${env.BUILD_URL}">Console Output</a></li>
                                <li><a href="${env.BUILD_URL}cucumber-html-reports/overview-features.html">Rapport Cucumber</a></li>
                                <li><a href="${env.BUILD_URL}Javadoc/">Documentation Javadoc</a></li>
                            </ul>

                            <p>Cordialement,<br/>Jenkins CI/CD</p>
                        </body>
                        </html>
                        """,
                        mimeType: 'text/html',
                        from: 'mr_asbar@esi.dz'
                    )
                }

                echo 'Email de notification envoyé'

                // Notification Slack (optionnel - si configuré)
                // slackSend (
                //     color: 'good',
                //     message: "Déploiement réussi - ${PROJECT_NAME} v${PROJECT_VERSION}\nBuild: #${env.BUILD_NUMBER}"
                // )
            }
        }
    }

    // ============================================
    // POST ACTIONS (Gestion des échecs)
    // ============================================
    post {
        failure {
            echo '========== Build Failed =========='

            emailext (
                to: 'asbarroufaida@gmail.com',
                subject: "Échec du build - ${PROJECT_NAME} #${env.BUILD_NUMBER}",
                body: """
                <html>
                <body>
                    <h2 style="color: red;">Échec du build</h2>
                    <p>Bonjour,</p>
                    <p>Le pipeline Jenkins a échoué.</p>

                    <h3>Détails:</h3>
                    <ul>
                        <li><strong>Projet:</strong> ${PROJECT_NAME}</li>
                        <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
                        <li><strong>Branch:</strong> ${env.BRANCH_NAME}</li>
                        <li><strong>Date:</strong> ${new Date().format('dd/MM/yyyy HH:mm:ss')}</li>
                    </ul>

                    <p><a href="${env.BUILD_URL}console">Voir les logs complets</a></p>

                    <p>Cordialement,<br/>Jenkins CI/CD</p>
                </body>
                </html>
                """,
                mimeType: 'text/html',
                from: 'mr_asbar@esi.dz'
            )

            // slackSend (
            //     color: 'danger',
            //     message: "Build échoué - ${PROJECT_NAME} #${env.BUILD_NUMBER}\n${env.BUILD_URL}"
            // )
        }

        success {
            echo '========== Build Successful =========='
        }

        always {
            echo '========== Pipeline termine =========='
            // Nettoyage si nécessaire
            cleanWs(
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true,
                patterns: [[pattern: 'build/**', type: 'INCLUDE']]
            )
        }
    }
}