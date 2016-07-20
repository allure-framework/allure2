def String project = "${PROJECT}";
def String orgName = (project =~ /.*[\/](.*)/)[0][0]
def String projectName = (project =~ /.*[\/](.*)/)[0][1]

mavenJob(projectName + '_pull-request') {
    label('java')

    scm {
        git {
            remote {
                github(project)
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }

    triggers {
        githubPullRequest {
            orgWhitelist([orgName])
            useGitHubHooks()
            permitAll()
        }
    }

    goals 'org.jacoco:jacoco-maven-plugin:0.7.4.201502262128:prepare-agent clean verify'

    publishers {
        slackNotifier {
            notifySuccess(true)
            notifyAborted(true)
            notifyFailure(true)
            notifyNotBuilt(true)
            notifyUnstable(true)
            notifyBackToNormal(true)

            room('#builds')
            commitInfoChoice('NONE')
            notifyRepeatedFailure(false)
            includeCustomMessage(false)
            includeTestSummary(false)
            startNotification(false)
            buildServerUrl(null)
            customMessage(null)
            teamDomain(null)
            authToken(null)
            sendAs(null)
        }
    }
}

mavenJob(projectName + '_deploy') {
    label('java')

    scm {
        git {
            remote {
                github project
            }
            branch('master')
            extensions {
                mergeOptions {
                    remote('origin')
                    branch('master')
                }
            }
        }
    }

    triggers {
        githubPush()
    }

    wrappers {
        mavenRelease {
            releaseGoals('release:clean release:prepare release:perform')
            dryRunGoals('-DdryRun=true release:prepare')
            numberOfReleaseBuildsToKeep(10)
        }
    }

    goals 'org.jacoco:jacoco-maven-plugin:0.7.4.201502262128:prepare-agent clean deploy'


    publishers {
        slackNotifier {
            notifySuccess(true)
            notifyAborted(true)
            notifyFailure(true)
            notifyNotBuilt(true)
            notifyUnstable(true)
            notifyBackToNormal(true)

            room('#builds')
            commitInfoChoice('NONE')
            notifyRepeatedFailure(false)
            includeCustomMessage(false)
            includeTestSummary(false)
            startNotification(false)
            buildServerUrl(null)
            customMessage(null)
            teamDomain(null)
            authToken(null)
            sendAs(null)
        }
    }
}