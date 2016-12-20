#!/usr/bin/env groovy

node('java') {

    stage('Checkout') {
        checkout scm
    }

    stage('Build and Test') {
        env.PATH = "${tool 'maven'}/bin:${env.PATH}"
        sh 'mvn clean package'
    }
}