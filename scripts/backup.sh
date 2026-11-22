#!/usr/bin/env bash
# Backup dos volumes (Jenkins, Grafana, Prometheus, Loki) em backups/<data>/*.tar.gz
# Uso: ./scripts/backup.sh
set -euo pipefail
cd "$(dirname "$0")/.."
destino="backups/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$destino"
for vol in jenkins_home grafana_data prometheus_data loki_data; do
  docker run --rm -v "devops-observability_${vol}:/dados:ro" -v "$(pwd)/${destino}:/backup" alpine \
    tar czf "/backup/${vol}.tar.gz" -C /dados .
  echo "ok: ${destino}/${vol}.tar.gz"
done
