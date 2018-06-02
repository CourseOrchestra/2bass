@Library('ratcheting') _
node {
    def server = Artifactory.server 'ART'
    def rtMaven = Artifactory.newMavenBuild()
    def buildInfo
    def oldWarnings

    stage ('Clone') {
        checkout scm
    }

    stage ('Artifactory configuration') {
        rtMaven.tool = 'M3' 
        rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
        rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server
        buildInfo = Artifactory.newBuildInfo()
        buildInfo.env.capture = true

        def downloadSpec = """
                 {"files": [
                    {
                      "pattern": "warn/2bass/*/warnings.yml",
                      "build": "2bass :: dev/LATEST",
                      "target": "previous.yml",
                      "flat": "true"
                    }
                    ]
                }"""
        // server.download spec: downloadSpec
        // oldWarnings = readYaml file: 'previous.yml'
    }

    stage ('Docker cleanup') {
        sh '''docker ps -a -q &> /dev/null
if [ $? != 0 ]; then
   docker rm $(docker ps -a -q)
fi'''
    }

    try{
        stage ('Exec Maven') {
            rtMaven.run pom: 'pom.xml', goals: 'clean install', buildInfo: buildInfo
        }
    } finally {
        junit 'target/surefire-reports/**/*.xml'
        checkstyle pattern: '**/target/checkstyle-result.xml'
        findbugs pattern: '**/target/spotbugsXml.xml'
    }

    stage ('Ratcheting') {
        def warningsMap = countWarnings
        println (warningsMap)
        writeYaml file: 'target/warnings.yml', data: warningsMap
        // compareWarningMaps oldWarnings, warningsMap
    }

    if (env.BRANCH_NAME == 'dev') {
        stage ('Publish build info') {
            def uploadSpec = """
            {
             "files": [
                {
                  "pattern": "target/warnings.yml",
                  "target": "warn/2bass/${currentBuild.number}/warnings.yml"
                }
                ]
            }"""

            def buildInfo2 = server.upload spec: uploadSpec
            buildInfo.append(buildInfo2)
            server.publishBuildInfo buildInfo
        }
    }
}
