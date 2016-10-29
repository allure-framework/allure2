node ('java') {

    stage('Checkout') {
        checkout scm
    }

    stage('Build and Test') {
        env.PATH = "${tool 'Maven 3'}/bin:${env.PATH}"
        sh 'mvn clean package'
    }
}