pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        sh '''
           echo "PATH = ${PATH}"
           echo "M2_HOME = ${M2_HOME}"
           echo "JAVA_HOME = ${JAVA_HOME}"
           mvn --version 
           '''
      }
    }
    stage('Build') {
      steps {
        sh 'mvn -Dmaven.test.failure.ignore=true install'
      }
      post {
        success {
          junit 'target/surefire-reports/**/*.xml'
        }
      }
    }
  }
  tools {
    maven 'Maven 3.5.3'
    jdk 'jdk8'
  }
  post {
    always {
    deleteDir()
    }
  }
}