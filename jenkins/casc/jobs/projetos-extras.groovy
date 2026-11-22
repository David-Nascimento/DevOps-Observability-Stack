// Jobs gerados a partir da lista abaixo: projetos de teste dos repositórios do GitHub (David-Nascimento).
// Para incluir outro projeto, acrescente um item na lista e use um dos modelos (tipo): cypress, node, ruby.

def github = 'https://github.com/David-Nascimento/'

// Pós-build comum: JUnit + Allure (com histórico) + arquivamento.
// @@JUNIT@@ = padrão dos XML; @@ALLURE@@ = pasta de resultados do Allure.
def posBuild = '''
  post {
    always {
      junit allowEmptyResults: true, testResults: '@@JUNIT@@'
@@COPIA@@
      sh 'mkdir -p "$JENKINS_HOME/allure-history/$JOB_NAME" && mkdir -p @@ALLURE@@ && rm -rf @@ALLURE@@/history && cp -r "$JENKINS_HOME/allure-history/$JOB_NAME" @@ALLURE@@/history || true'
      sh 'rm -rf allure-report && allure generate @@ALLURE@@ --clean -o allure-report || true'
      sh 'cp -r allure-report/history/. "$JENKINS_HOME/allure-history/$JOB_NAME/" || true'
      publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'allure-report', reportFiles: 'index.html', reportName: 'Allure Report'])
    }
  }
'''

def modelo = { String repo, String instalar, String executar, String junit, String allure, boolean copiaXml, String envVars ->
  def copia = copiaXml ? "      sh 'mkdir -p allure-results && cp " + junit + " allure-results/ 2>/dev/null || true'\n" : ''
  def texto = '''
pipeline {
  agent any
  options {
    timestamps()
    timeout(time: 25, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }
@@ENVBLOCK@@  stages {
    stage('Checkout') {
      steps {
        git url: '@@URL@@', branch: 'main'
      }
    }
    stage('Dependencias') {
      steps {
        sh '@@INSTALAR@@'
      }
    }
    stage('Testes') {
      steps {
        sh '@@EXECUTAR@@'
      }
    }
  }
''' + posBuild + '''
}
'''
  def envBloco = envVars ? "  environment {\n    " + envVars + "\n  }\n" : ''
  texto = texto.replace('@@ENVBLOCK@@', envBloco)
  texto = texto.replace('@@COPIA@@\n', copia)
  texto = texto.replace('@@JUNIT@@', junit).replace('@@ALLURE@@', allure)
  texto = texto.replace('@@URL@@', github + repo).replace('@@INSTALAR@@', instalar).replace('@@EXECUTAR@@', executar)
  return texto
}

def projetos = [
  [
    nome: 'cypress-api-serverest', repo: 'desafio-api-qa',
    desc: 'Testes de API com Cypress contra o ServeRest (login, usuarios, produtos e carrinhos) no ServeRest local do compose',
    instalar: 'npm ci', executar: 'npx cypress run --reporter junit --reporter-options mochaFile=results/junit-[hash].xml',
    junit: 'results/*.xml', allure: 'allure-results', copiaXml: true,
    env: "CYPRESS_BASE_URL = 'http://serverest:3000'"
  ],
  [
    nome: 'playwright-cronicas-battleon', repo: 'cronicas-de-battleon',
    desc: 'Build, checagem de tipos e testes E2E com Playwright do portal Cronicas de Battleon',
    instalar: 'npm ci && npx playwright install chromium', executar: 'npm run build && npm run typecheck && npm test',
    junit: 'nao-gera-junit/*.xml', allure: 'allure-results', copiaXml: false, env: ''
  ],
  [
    nome: 'qa-playground-checks', repo: 'QA-Playground',
    desc: 'Build e verificacoes de qualidade (DOM, links e contrato da API mock) do QA Playground',
    instalar: 'npm ci', executar: 'npm run qa',
    junit: 'nao-gera-junit/*.xml', allure: 'allure-results', copiaXml: false, env: ''
  ],
  [
    nome: 'ruby-api-httparty', repo: 'Httparty-Cucumber-Structury',
    desc: 'Testes de API em Ruby (HTTParty + Cucumber) contra o Petstore',
    instalar: 'bundle config set --local path vendor/bundle && bundle install --jobs 4',
    executar: 'rm -rf allure-results reports && mkdir -p reports/junit && bundle exec cucumber --format junit --out reports/junit',
    junit: 'reports/junit/*.xml', allure: 'allure-results', copiaXml: false, env: ''
  ],
  [
    nome: 'ruby-unit-rspec', repo: 'unit-testes-ruby',
    desc: 'Testes unitarios em Ruby com RSpec (exemplo de dominio bancario)',
    instalar: 'bundle config set --local path vendor/bundle && bundle install --jobs 4',
    executar: 'mkdir -p reports && bundle exec rspec --format progress --format RspecJunitFormatter --out reports/rspec.xml',
    junit: 'reports/*.xml', allure: 'allure-results', copiaXml: true, env: ''
  ],
]

projetos.each { p ->
  pipelineJob(p.nome) {
    description(p.desc + ' (' + github + p.repo + ')')
    definition {
      cps {
        sandbox(true)
        script(modelo(p.repo, p.instalar, p.executar, p.junit, p.allure, p.copiaXml, p.env).stripIndent())
      }
    }
  }
}
