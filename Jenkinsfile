def gitUrl = 'https://github.com/imavroukakis/gatling-scale-out'
def gitCredentials = 'Github'
def testGroups = [:]
def numberOfTestNodes = 5
def splitTestsAbove = 50.0
def jdkTool = 'openjdk-11'
def sbtTool = '1.3.8'

pipeline {
    agent any
    tools {
        jdk jdkTool
    }
    environment {
        SBT_HOME = tool name: sbtTool, type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'
        PATH = "${env.SBT_HOME}/bin:${env.PATH}"
    }

    parameters {
        choice(choices: ['5', '10', '15', '20', '30', '40', '50', '60', '70', '80', '90', '100'], description: 'The amount of users per second to generate', name: 'usersPerSecond')
        choice(choices: ['1_minute', '2_minutes', '5_minutes', '10_minutes', '15_minutes', '20_minutes'], description: 'The amount of time to run the simulation for', name: 'duration')
    }
    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                git branch: 'main', credentialsId: "$gitCredentials", poll: false, url: "$gitUrl"
            }
        }
        stage('Build') {
            steps {
                sh "sbt clean compile packArchiveTgz"
                stash name: 'load-test', includes: 'target/gatling-scale-out-1.0.tar.gz'
            }
        }
        stage('Load Test') {
            steps {
                script {
                    currentBuild.description = "Users/sec:${params.usersPerSecond}/Duration:${params.duration}"
                    def userPerSecond = "${params.usersPerSecond}" as Double
                    int usersPerNodeCount
                    if (userPerSecond >= splitTestsAbove) {
                        usersPerNodeCount = Math.round(userPerSecond / numberOfTestNodes)
                    } else {
                        usersPerNodeCount = userPerSecond
                        numberOfTestNodes = 1
                    }
                    for (int i = 0; i < numberOfTestNodes; i++) {
                        def num = i
                        testGroups["node $num"] = {
                            node {
                                def javaHome = tool name: jdkTool
                                deleteDir()
                                unstash 'load-test'
                                sh 'mv target/gatling-scale-out-1.0.tar.gz ./'
                                sh 'tar xf gatling-scale-out-1.0.tar.gz'
                                sh "JAVA_HOME=$javaHome gatling-scale-out-1.0/bin/load-test --users-per-second=$usersPerNodeCount --test-duration=${params.duration}"
                                stash name: "node $num", includes: '**/simulation.log'
                            }
                        }
                    }
                    parallel testGroups
                }
            }
        }
        stage('Collect results') {
            steps {
                script {
                    for (int i = 0; i < numberOfTestNodes; i++) {
                        def num = i
                        unstash "node $i"
                    }
                }
                sh 'mv target/gatling-scale-out-1.0.tar.gz ./'
                sh 'tar xf gatling-scale-out-1.0.tar.gz'
                sh "gatling-scale-out-1.0/bin/load-test --report-only \"${env.WORKSPACE}/results\""
                sh "mv results results-test-${env.BUILD_NUMBER}"
                sh "tar zcf results-test-${env.BUILD_NUMBER}.tar.gz results-test-${env.BUILD_NUMBER}"
                archiveArtifacts artifacts: "results-test-${env.BUILD_NUMBER}.tar.gz", caseSensitive: false, onlyIfSuccessful: true
            }
        }
    }
}
