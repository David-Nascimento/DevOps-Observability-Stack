// Job de pipeline criado automaticamente: roda o ruby-e2e-framework.
// A UI usa o Selenium Grid do docker-compose (serviço "selenium"); API e acessibilidade rodam direto.
// Relatório: Allure Report (aba no menu lateral do build).
pipelineJob('ruby-e2e-tests') {
  description('Testes E2E em Ruby (https://github.com/David-Nascimento/ruby-e2e-framework)')
  triggers {
    // Verifica o repositório a cada ~10 minutos e roda quando houver commit novo
    scm('H/10 * * * *')
  }
  parameters {
    choiceParam('PERFIL', ['ci', 'api', 'smoke', 'a11y', 'regression'], 'Perfil do Cucumber (config/cucumber.yml). ci = tudo; api = sem navegador')
  }
  definition {
    cps {
      sandbox(true)
      script('''
pipeline {
  agent any
  options {
    timestamps()
    timeout(time: 30, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }
  environment {
    REMOTE_URL = 'http://selenium:4444/wd/hub'
    BROWSER = 'chrome'
    HEADLESS = 'true'
    BUNDLE_PATH = 'vendor/bundle'
  }
  stages {
    stage('Checkout') {
      steps {
        git url: 'https://github.com/David-Nascimento/ruby-e2e-framework', branch: 'main'
      }
    }
    stage('Dependencias') {
      steps {
        sh 'bundle install --jobs 4'
      }
    }
    stage('Testes E2E') {
      steps {
        // O perfil "ci" já inclui o formatador do Allure; nos demais ele é acrescentado aqui.
        sh 'rm -rf reports && mkdir -p reports/junit && bundle exec cucumber -p ' + params.PERFIL + ' --format junit --out reports/junit' + (params.PERFIL == 'ci' ? '' : ' --format AllureCucumber::CucumberFormatter --out reports/allure-results')
      }
    }
  }
  post {
    always {
      junit allowEmptyResults: true, testResults: 'reports/junit/*.xml'
      sh 'mkdir -p "$JENKINS_HOME/allure-history/$JOB_NAME" && rm -rf reports/allure-results/history && cp -r "$JENKINS_HOME/allure-history/$JOB_NAME" reports/allure-results/history || true'
      sh 'rm -rf allure-report && allure generate reports/allure-results --clean -o allure-report || true'
      sh 'cp -r allure-report/history/. "$JENKINS_HOME/allure-history/$JOB_NAME/" || true'
      publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'allure-report', reportFiles: 'index.html', reportName: 'Allure Report'])
      archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
    }
  }
}
'''.stripIndent())
    }
  }
}
