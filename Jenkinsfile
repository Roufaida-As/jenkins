pipeline {
    agent any
    tools {
            jdk 'JDK11'
    }

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
                bat 'gradlew clean test'

                // Étape 1.2: Archivage des résultats des tests
                echo 'Archivage des resultats de tests...'
                junit '**/build/test-results/test/*.xml'

                // Étape 1.3: Génération des rapports Cucumber
                echo 'Generation des rapports Cucumber...'
                bat 'gradlew generateCucumberReports'
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
                bat 'gradlew compileJava sonar'
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

               timeout(time: 10, unit: 'MINUTES') {
                           script {
                               def qg = waitForQualityGate()
                               if (qg.status != 'OK') {
                                   error "Pipeline aborted due to quality gate failure: ${qg.status}"
                               }
                           }
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
                bat 'gradlew build -x test'

                // Étape 4.2: Génération de la documentation
                echo 'Generation de la Javadoc...'
                bat 'gradlew generateJavadoc'

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
                    bat 'gradlew publish'
                }

                echo "Deploiement reussi sur ${MAVEN_REPO_URL}"
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
                        to: 'asbarroufaida@gmail.com',
                        replyTo: 'mr_asbar@esi.dz',
                        subject: "Deploiement reussi - ${PROJECT_NAME} v${PROJECT_VERSION}",
                        body: """
                        <html>
                        <body>
                            <h2 style="color: green;">Deploiement reussi</h2>
                            <p>Bonjour,</p>
                            <p>Le deploiement de la librairie a ete effectue avec succes par <strong>ASBAR ROUFAIDA</strong>.</p>

                            <h3>Details du deploiement:</h3>
                            <ul>
                                <li><strong>Projet:</strong> ${PROJECT_NAME}</li>
                                <li><strong>Version:</strong> ${PROJECT_VERSION}</li>
                                <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
                                <li><strong>Date:</strong> ${new Date().format('dd/MM/yyyy HH:mm:ss')}</li>
                                <li><strong>Branch:</strong> ${env.BRANCH_NAME}</li>
                            </ul>

                            <h3>Quality Gate SonarQube:</h3>
                            <p><a href="http://localhost:9000/dashboard?id=TP7-API-INTEGRATION">Voir l'analyse SonarQube</a></p>

                            <h3>Repository Maven:</h3>
                            <p><a href="${MAVEN_REPO_URL}">${MAVEN_REPO_URL}</a></p>

                            <h3>Utilisation:</h3>
                            <pre>implementation "asbar-roufaida:${PROJECT_NAME}:${PROJECT_VERSION}"</pre>

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
                        mimeType: 'text/html'
                    )
                }
                echo 'Email de notification envoye'


                // Notification Slack
               slackSend (
                   baseUrl: 'https://hooks.slack.com/services/',
                   tokenCredentialId: 'slack-webhook', // Force l'utilisation de votre secret
                   channel: '#jenkins',
                   color: 'good',
                   message: """
                       *Deploiement reussi*
                       *Projet* : ${PROJECT_NAME}
                       *Version* : ${PROJECT_VERSION}
                       *Build* : #${env.BUILD_NUMBER}
                       *Branch* : ${env.BRANCH_NAME}
                       Lien : ${env.BUILD_URL}
                   """
               )

                echo 'Slack de notification envoye'
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
                replyTo: 'mr_asbar@esi.dz',
                subject: "Echec du build - ${PROJECT_NAME} #${env.BUILD_NUMBER}",
                body: """
                <html>
                <body>
                    <h2 style="color: red;">Echec du build</h2>
                    <p>Bonjour,</p>
                    <p>Le pipeline Jenkins a echoue.</p>

                    <h3>Details:</h3>
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
                mimeType: 'text/html'
            )

            slackSend (
                baseUrl: 'https://hooks.slack.com/services/',
                tokenCredentialId: 'slack-webhook', // Force l'utilisation de votre secret

                channel: '#jenkins',
                color: 'danger',
                message: """
            *Build échoué*
            *Projet* : ${PROJECT_NAME}
            *Build* : #${env.BUILD_NUMBER}
            *Branch* : ${env.BRANCH_NAME}
             Logs : ${env.BUILD_URL}console
            """
            )


            echo 'Email et slack d\'echec envoye'
        }

        success {
            echo '========== Build Successful =========='
        }

        always {
            echo '========== Pipeline termine =========='
            // Only clean if the build was successful to allow debugging on failure
            script {
                if (currentBuild.result == 'SUCCESS') {
                    cleanWs(
                        deleteDirs: true,
                        patterns: [[pattern: 'build/**', type: 'INCLUDE']]
                    )
                }
            }
        }
    }
}