#!groovy
/*
Example pipeline
*/



agent("ubuntu") {
    stage('Build') {
        withEnv(['DOCKER_REGISTRY_USERNAME=bobbytables',
                'DOCKER_REGISTRY_PASSWORD=CorrectHorseBatteryStaple',
                'DOCKER_REGISTRY_API_AUTH_TOKEN =khaaaaaaaaan+3f2ee']) {
        echo "In Build Stage"

        checkout scm
        sh """
            build_app.sh
            nosetests "./tests/unit/"
        """
        }
    }

    stage('Deploy') {
        withEnv(["AWS_ACCESS_KEY_ID=1234566789",
                "AWS_SECRET_ACCESS_KEY=3EL45adfINJDR/ad43DD/+FAKE"
                "AWS_DEFAULT_REGION=us-east-1"
        ])
            sh "deploy_to_test.sh"
    }

    stage('UI Tests'){
        echo "In UI Tests Stage. Running Tests."

        echo "running Tests"
        sh "uiTest_ChromeWin.sh"
        sh "uiTest_ChromeOSX.sh"
        sh "uiTest_FirefoxWin.sh"
        sh "uiTest_FirefoxOSX.sh"

        junit 'reports/**/*.xml'
    }

    stage("Integration Tests") {
            echo "In Integration Tests Stage"

        // Reminder: Configure job to dis-allow concurrent builds, because
        // DB setup needs to run isolated

        sh """
            setup_test_database.sh
            integration_tests.sh
        """
        junit 'reports/**/*.xml'
    }

    stage("send notifications") {
        def buildStatus = currentBuild.result ?: "Success"
        def mail_subject = "${buildStatus}: ${JOB_NAME} #${BUILD_NUMBER}"
        def mail_body = """\
Jenkins ${buildStatus} ${JOB_NAME} #${BUILD_NUMBER} ${buildStatus}
View build log: ${BUILD_URL}consoleFull"""


        echo "Sending mail to: ${mail_to} with subject: ${mail_subject}; body: ${mail_body}"

        mail from: "jenkins@demo.test"
            to:"engineering@demo.text"
            subject: "Deploy to Test done"
            body: "Check the build logs for results"
    }
}
