#!/usr/bin/env bash
# Restaura um volume a partir de um backup. Pare a stack antes: docker compose down
# Uso: ./scripts/restore.sh backups/20261122-130000 jenkins_home
set -euo pipefail
cd "$(dirname "$0")/.."
pasta="${1:?informe a pasta do backup}"; vol="${2:?informe o volume (ex.: jenkins_home)}"
docker run --rm -v "devops-observability_${vol}:/dados" -v "$(pwd)/${pasta}:/backup:ro" alpine \
  sh -c "rm -rf /dados/* /dados/.[!.]* 2>/dev/null; tar xzf /backup/${vol}.tar.gz -C /dados"
echo "restaurado: ${vol}"
