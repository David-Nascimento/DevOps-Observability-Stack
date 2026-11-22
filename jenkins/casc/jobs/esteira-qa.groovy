// Esteira de QA: dispara os testes de API (Karate) e E2E (Ruby) em paralelo.
// Roda de segunda a sexta de manhã e pode ser disparada manualmente.
pipelineJob('esteira-qa') {
  description('Esteira completa: Karate (API) + Ruby E2E (API, UI e acessibilidade) em paralelo')
  triggers {
    cron('H 6 * * 1-5')
  }
  parameters {
    stringParam('TAGS_KARATE', '~@wip', 'Filtro de tags do Karate, ex.: @smoke')
    booleanParam('INCLUIR_EXTRAS', false, 'Roda tambem os projetos extras (Cypress API, Playwright, QA Playground, Ruby API e RSpec), um por vez')
    choiceParam('PERFIL_RUBY', ['ci', 'api', 'smoke', 'a11y', 'regression'], 'Perfil do Cucumber no Ruby E2E')
  }
  definition {
    cps {
      sandbox(true)
      script('''
pipeline {
  agent none
  options {
    timestamps()
    timeout(time: 45, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }
  stages {
    stage('Testes') {
      parallel {
        stage('API - Karate') {
          steps {
            build job: 'karate-api-tests', wait: true, propagate: true,
                  parameters: [string(name: 'TAGS', value: params.TAGS_KARATE)]
          }
        }
        stage('E2E - Ruby') {
          steps {
            build job: 'ruby-e2e-tests', wait: true, propagate: true,
                  parameters: [string(name: 'PERFIL', value: params.PERFIL_RUBY)]
          }
        }
      }
    }
    stage('Projetos extras') {
      when { expression { params.INCLUIR_EXTRAS } }
      stages {
        stage('Cypress API') {
          steps { catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') { build job: 'cypress-api-serverest', wait: true, propagate: true } }
        }
        stage('Playwright - Cronicas') {
          steps { catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') { build job: 'playwright-cronicas-battleon', wait: true, propagate: true } }
        }
        stage('QA Playground') {
          steps { catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') { build job: 'qa-playground-checks', wait: true, propagate: true } }
        }
        stage('Ruby API') {
          steps { catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') { build job: 'ruby-api-httparty', wait: true, propagate: true } }
        }
        stage('Ruby RSpec') {
          steps { catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') { build job: 'ruby-unit-rspec', wait: true, propagate: true } }
        }
      }
    }
  }
}
'''.stripIndent())
    }
  }
}
