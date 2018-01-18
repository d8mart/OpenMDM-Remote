PROPERTYFILE = 'app/version.properties'

this.props = null
this.PATH_VERSIONS = "/home/webkey/versions"
this.SERVER_ADDRESS = "webkey.cc"
this.ANDROID_PLATFORM_DIR = "android-repos"
node {
    try {
        currentBuild.result = 'SUCCESS'
        setProperties()
        deleteDir()

        stage('checkout and set version') {
            checkout scm
            readAndSetProperties()
            setDisplayName()
        }
        stage('build native') {
            writeNativeBuildConfig()

            if (hasCache()) {
                copyNativeArtifactFromCache()
            } else {
                prepareNativeCopy()
                buildNative()
            }
        }
        stage('build apk') {
            prepareSigningKey()
            buildAPK()
            archiveArtifacts 'app/build/outputs/apk/**/*.apk'
        }
        stage("deploy webkit to ${SERVER_ADDRESS}") {
            uploadWebkitAssets()
            deployWebkit()
        }
        stage('publish APK') {
            sh "find app/build/outputs/apk/ -name '*.apk' -exec rsync --progress -ave ssh {} deploy@${SERVER_ADDRESS}:/home/webkey/www/${SERVER_ADDRESS}/public/ \\;"
        }
    } catch (Exception err) {
        currentBuild.result = 'FAILURE'
        throw err
    } finally {
      stage('send notification') {
        sendNotification(currentBuild.result)
      }
    }

}

def sendNotification(res) {
    def msg
    def color

    props = readPropertiesFromFile()

    if(res == 'FAILURE') {
        color = '#FF0000'
        msg = "Failed to build '${env.JOB_NAME}'"
    } else {
        msg = "Job '${env.JOB_NAME}' ('${env.BUILD_NUMBER}') is done. "
    }
    slackSend color: color, message: msg
}

def hasCache() {
    File cacheDir =  new File(env.NATIVE_CACHE);
    if(cacheDir.isDirectory() && cacheDir.list().length == 0) {
        return false;
    } else {
        return true;
    }
}

def copyNativeArtifactFromCache() {
    build_dir = "${WORKSPACE}/backend/out"
    sh "mkdir $build_dir && cp -a $env.NATIVE_CACHE/* $build_dir"
}

def readAndSetProperties() {
  props = readPropertiesFromFile()
  props.setProperty("VERSION_CODE", env.BUILD_ID)
  writeFile file: PROPERTYFILE, text: writeProperties(props)
}

def setProperties() {
    def jprops = []
    jprops.add([$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '8']])
    jprops.add(disableConcurrentBuilds())
    properties(jprops)
}
 
def buildNative() {
    sh "cd backend && ./build.py -a"
}

def prepareNativeCopy() {
    def myAndroidRepos = "${WORKSPACE}/${ANDROID_PLATFORM_DIR}"

    sh "cp -al ${ANDROID_PLATFORMS_HOME} ${WORKSPACE}"
}

def writeNativeBuildConfig() {
    def configFile = "${WORKSPACE}/backend/build_config.py"
    def myAndroidRepos = "${WORKSPACE}/${ANDROID_PLATFORM_DIR}"

    sh '''
cat << EOF > $WORKSPACE/backend/build_config.py
OUT_PATH = "./out"
JAVA_6_PATH = "/usr/lib/jvm/oracle-java6-jdk-amd64"
JAVA_7_PATH = "/usr/lib/jvm/java-7-openjdk-amd64"
JAVA_8_PATH = "/usr/lib/jvm/java-1.8.0-openjdk-amd64"
EOF
'''
    sh "echo \"BACKENDROOT = '${myAndroidRepos}'\" >> ${configFile}"
}

def prepareSigningKey() {
    sh 'ln -sf /var/lib/jenkins/keys "${WORKSPACE}"'
}

def buildAPK() {
    sh "./gradlew assembleRelease"
}

def uploadWebkitAssets() {
    def vcode = props.getProperty('VERSION_CODE')
    def vFolder = PATH_VERSIONS+"/"+vcode

    sh "ssh -p 8822 deploy@${SERVER_ADDRESS} 'rm -rf ${vFolder}/* && mkdir -p ${vFolder}'"
    sh "ssh -p 8822 deploy@${SERVER_ADDRESS} rm -rf ${PATH_VERSIONS}/0/*"

    sh "scp -P8822 -r app/src/main/assets/webkit/* deploy@${SERVER_ADDRESS}:${PATH_VERSIONS}/${vcode}"
    sh "scp -P8822 -r app/src/main/assets/webkit/* deploy@${SERVER_ADDRESS}:${PATH_VERSIONS}/0"
}

def deployWebkit() {
    sh "ssh -p 8822 deploy@${SERVER_ADDRESS} 'harbor --monitor.port 1235  --lighthouse.action=reloadcache'"
}

def readPropertiesFromFile () {
    def str = readFile file: PROPERTYFILE, charset : 'utf-8'
    def sr = new StringReader(str)
    def props = new Properties()
    props.load(sr)
    return props
}

def setDisplayName() {
    currentBuild.displayName =
        "#" + props.getProperty('VERSION_CODE') + ", " +
        props.getProperty('VERSION_NAME')
}

@NonCPS def writeProperties (props) {
    def sw = new StringWriter()
    props.store(sw, null)
    return sw.toString()
}
