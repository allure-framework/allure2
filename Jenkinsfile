pipeline {
    agent { label 'java' }
    stages {
        stage("Build") {
            steps {
                sh './gradlew build'
            }
        }
        stage("Reports") {
            steps {
                checkstyle pattern: '**/build/reports/checkstyle/main.xml', defaultEncoding: 'UTF8',
                        canComputeNew: false, healthy: '', unHealthy: ''
                findbugs pattern: '**/build/reports/findbugs/main.xml', defaultEncoding: 'UTF8',
                        canComputeNew: false, healthy: '', unHealthy: '', excludePattern: '', includePattern: ''
                pmd pattern: '**/build/reports/pmd/main.xml', defaultEncoding: 'UTF8',
                        canComputeNew: false, healthy: '', unHealthy: ''
            }
        }
        stage("Demo") {
            steps {
                sh 'allure-commandline/build/install/allure/bin/allure generate ' +
                        'allure-generator/test-data/demo --clean -o build/report-demo'
                publishHTML([reportName  : 'Demo Report', reportDir: 'build/report-demo', reportFiles: 'index.html',
                             reportTitles: '', allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false])

                sh 'allure-commandline/build/install/allure/bin/allure generate ' +
                        'allure-generator/test-data/demo2 --clean -o build/report-demo2'
                publishHTML([reportName  : 'Demo2 Report', reportDir: 'build/report-demo2', reportFiles: 'index.html',
                             reportTitles: '', allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false])
            }
        }
    }
    post {
        failure {
            slackSend message: "${env.JOB_NAME} - #${env.BUILD_NUMBER} failed (<${env.BUILD_URL}|Open>)",
                    color: 'danger', teamDomain: 'qameta', channel: 'allure', tokenCredentialId: 'allure-channel'
        }
    }
}