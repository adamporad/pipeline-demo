#!groovy
package com.pipelinedemo

def send_build_notifications(buildStatus, message=null, alwaysSendEmailOnSuccess=false, to_addr=null) {
    send_email(buildStatus, message, alwaysSendEmailOnSuccess, to_addr)
    send_hipchat_message(buildStatus, message)
}


def send_email(buildStatus, message=null, alwaysSendEmailOnSuccess=false, to_addr=null) {
    buildStatus =  buildStatus ?: 'SUCCESS'

    // Only send email on the first SUCCESSful build after a FAILURE.
    //     Unless sendOnSuccess parameter is true
    // previousBuild = currentBuild.getPreviousBuild()
    previousBuildResult = previousBuild ? previousBuild.result : ""
    if ((buildStatus == 'SUCCESS')
            && (previousBuildResult == 'SUCCESS')
            && (alwaysSendEmailOnSuccess == false)) {

        return
    }

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

def send_hipchat_message(buildStatus, message=null) {
    buildStatus =  buildStatus ?: 'SUCCESS'

    //set message color based on build status
    color = (buildStatus == 'SUCCESS') ? 'GREEN' : 'RED'

    def hipchat_message = """${buildStatus} Job <a href='${env.BUILD_URL}console'>${JOB_NAME}&nbsp;#${BUILD_NUMBER}</a> has finished."""
    if (message != null)
        hipchat_message = """${hipchat_message}\n\n${message}"""

    hipchatSend (color: color, notify: true, message: hipchat_message)
}

def send_datadog_event(buildStatus, message=null) {
    buildStatus = buildStatus ?: 'SUCCESS'
    def alert_type = (buildStatus == 'SUCCESS') ? "info" : "error"
    def event_title = "Receipt Report Job # ${BUILD_NUMBER} ${buildStatus}"
    def event_text = """\
${JOB_NAME} # ${BUILD_NUMBER} ${buildStatus}
[View build in Jenkins](${BUILD_URL})
${message ?: ''}"""

    def event = """{
                        "title": "${event_title}",
                        "text": "${event_text}",
                    	"priority": "normal",
                    	"tags": ["report:${JOB_NAME}"],
                    	"alert_type": "${alert_type}",
                    	"source_type_name": "my apps"
                    }
    """

    echo "Sending event to DataDog: ${event}"

    mail from: "jenkins@demo.test", to: "event-abcdef76@dtdg.co", subject: event_title, body: event
}

return this;
