def server = Artifactory.server 'Artifactory'
def rtMaven = Artifactory.newMavenBuild()

pipeline {
    agent any
    tools {
        maven 'maven'
        jdk 'Java 17'
    }

    environment {
        ARTIFACTORY_URL = 'http://192.168.1.27:8082/artifactory'
        ARTIFACTORY_CREDS_ID = 'cred_jfrog'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main2', url: 'https://github.com/LinaChalouati/BackMonitoring.git'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn --version'
                sh 'java --version'
                sh 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('Publish to Jfrog Artifactory') {

                  steps {
                    script {
                    def uploadSpec = """{
                        "files": [
                            {
                                "pattern": "target/*.jar",
                                "target": "pfeaes-Jenkins-snapshot/"
                            }
                        ]
                    }"""
                    server.upload(uploadSpec)
                    }
                  }

        }




    }
}
