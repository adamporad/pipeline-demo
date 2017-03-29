#!groovy
/*
Example pipeline
- @Library
*/

@Library("pipeline-demo")
import com.pipeline-demo

try {
    def status_msg

    node("ubuntu") {
        stage('Build') {
            withCredentials(
                [usernamePassword(
                    credentialsId: 'My_Docker_registry',
                    usernameVariable: 'DOCKER_REGISTRY_USERNAME',
                    passwordVariable: 'REGISTRY_PASSWORD'),
                string(credentialsId: 'MY_DOCKER_REGISTRY_API_AUTH_TOKEN',
                    variable: 'DOCKER_REGISTRY_API_AUTH_TOKEN')
                ]) {

            echo "In Build Stage"
            checkout scm

            sh """
                build_app.sh
                nosetests "./tests/unit/"
            """
            }
        }

        checkpoint "Unittests Passed"

        stage('Deploy') {
            withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                credentialsId: 'My_AWS_Access_Key',
                                accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                                secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {

                echo "In Deploy Stage"
                sh "deploy_to_test.sh"
        }

        checkpoint "Environment Deployed"
        stage('UI Tests'){
            echo "running Tests in parallel"

            parallel
                chromeWin: { sh "uiTest_ChromeWin.sh" },
                chromeOSX: { sh "uiTest_ChromeOSX.sh" },
                firefoxWin: { sh "uiTest_FirefoxWin.sh" },
                firefoxOSX: { sh "uiTest_FirefoxOSX.sh" },
                failFast: false

            junit 'reports/**/*.xml'
        }

        checkpoint "UI Tests Passed"
        stage("Integration Tests") {
            echo "In Integration Tests Stage"

            // Reminder: Configure job to dis-allow concurrent builds, because
            // DB setup needs to run isolated

            lock('test_db_setup_lock') {
                sh "setup_test_database.sh"
            }

            sh "integration_tests.sh"
            junit 'reports/**/*.xml'
        }
    }
}
catch(err) {
    status_msg = err.toString()
    currentBuild.result
    raise err
}
finally {
    notifier = new notifications()
    currentBuild.result = currentBuild.result ?: "SUCCESS"
    notifier.send_email(currentBuild.result, message=status_msg)
}
