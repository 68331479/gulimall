pipeline {
  agent {
    node {
      label 'maven'
    }
  }

 parameters {
    string(name: 'PROJECT_VERSION', defaultValue: 'V0.0Beta', description: '')
    string(name: 'PROJECT_NAME', defaultValue: '', description: '需要构建的项目名称')
  }

 environment {
     DOCKER_CREDENTIAL_ID = 'dockerhub-id'
     GITEE_CREDENTIAL_ID = 'gitee-id'
     KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
     REGISTRY = 'docker.io'
     DOCKERHUB_NAMESPACE = '68331479'
     GITEE_ACCOUNT = 'ldeng12345'
     SONAR_CREDENTIAL_ID= 'sonar-qube'
 }

    stages {
      stage('拉取代码') {
        steps {
          git(url: 'https://gitee.com/ldeng12345/gulimall.git', credentialsId: 'gitee-id', branch: 'master', changelog: true, poll: false)
          sh 'echo 正在构建 $PROJECT_NAME 版本号码： $PROJECT_VERSION 将会提交给$REGISTRY 镜像仓库'
        }
      }
    }
  }

}