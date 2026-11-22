// Job de pipeline criado automaticamente: roda o karate-api-framework.
// Relatórios: Allure Report e Karate Report (abas no menu lateral do build).
pipelineJob('karate-api-tests') {
  description('Testes de API com Karate (https://github.com/David-Nascimento/karate-api-framework)')
  triggers {
    // Verifica o repositório a cada ~10 minutos e roda quando houver commit novo
    scm('H/10 * * * *')
  }
  parameters {
    stringParam('TAGS', '~@wip', 'Filtro de tags do Karate, ex.: @smoke')
    stringParam('AMBIENTE', 'dev', 'Ambiente: dev usa a API publica ServeRest')
    stringParam('THREADS', '2', 'Threads em paralelo')
  }
  definition {
    cps {
      sandbox(true)
      script('''
pipeline {
  agent any
  options {
    timestamps()
    timeout(time: 20, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }
  stages {
    stage('Checkout') {
      steps {
        git url: 'https://github.com/David-Nascimento/karate-api-framework', branch: 'main'
      }
    }
    stage('Testes de API') {
      steps {
        sh 'mvn -B test -Dkarate.env=' + params.AMBIENTE + ' -Dkarate.threads=' + params.THREADS + ' -Dkarate.options="--tags ' + params.TAGS + '"'
      }
    }
  }
  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml, target/karate-reports/*.xml'
      sh 'rm -rf allure-results allure-report && mkdir -p allure-results && cp target/karate-reports/*.xml allure-results/ 2>/dev/null || true'
      sh 'mkdir -p "$JENKINS_HOME/allure-history/$JOB_NAME" && rm -rf allure-results/history && cp -r "$JENKINS_HOME/allure-history/$JOB_NAME" allure-results/history || true'
      sh 'allure generate allure-results --clean -o allure-report || true'
      sh 'cp -r allure-report/history/. "$JENKINS_HOME/allure-history/$JOB_NAME/" || true'
      publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'allure-report', reportFiles: 'index.html', reportName: 'Allure Report'])
      publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'target/karate-reports', reportFiles: 'karate-summary.html', reportName: 'Karate Report'])
      archiveArtifacts artifacts: 'target/karate-reports/**', allowEmptyArchive: true
    }
  }
}
'''.stripIndent())
    }
  }
}
