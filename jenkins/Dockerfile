FROM jenkins/jenkins:lts-jdk21

USER root
# Maven (job do Karate), Ruby + compiladores (job do Ruby E2E: gems nativas) e curl.
# O navegador dos testes de UI roda no serviço "selenium" do docker-compose.
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven curl unzip ruby-full build-essential git \
    && gem install bundler --no-document \
    && rm -rf /var/lib/apt/lists/*

# Node.js 22 (jobs de Cypress, Playwright e dos projetos web) e bibliotecas do Cypress/Chromium
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y --no-install-recommends nodejs xvfb xauth libgtk-3-0 libgbm1 libnotify4 libnss3 libxss1 libxtst6 \
       $(apt-cache show libasound2t64 >/dev/null 2>&1 && echo libasound2t64 || echo libasound2) \
    && (npx -y playwright@latest install-deps chromium || echo "aviso: dependencias do Playwright nao instaladas") \
    && rm -rf /var/lib/apt/lists/*

# Allure Report (CLI): gera o relatório HTML a partir dos resultados dos testes
ARG ALLURE_VERSION=2.32.0
RUN curl -fsSL -o /tmp/allure.zip "https://repo.maven.apache.org/maven2/io/qameta/allure/allure-commandline/${ALLURE_VERSION}/allure-commandline-${ALLURE_VERSION}.zip" \
    && unzip -q /tmp/allure.zip -d /opt \
    && ln -s /opt/allure-${ALLURE_VERSION}/bin/allure /usr/local/bin/allure \
    && rm /tmp/allure.zip

USER jenkins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
