# Backup dos volumes (Jenkins, Grafana, Prometheus, Loki) em backups\<data>\*.tar.gz
# Uso: .\scripts\backup.ps1
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
$destino = Join-Path "backups" (Get-Date -Format "yyyyMMdd-HHmmss")
New-Item -ItemType Directory -Force -Path $destino | Out-Null
$abs = (Resolve-Path $destino).Path
foreach ($vol in "jenkins_home", "grafana_data", "prometheus_data", "loki_data") {
  docker run --rm -v "devops-observability_${vol}:/dados:ro" -v "${abs}:/backup" alpine tar czf "/backup/${vol}.tar.gz" -C /dados .
  Write-Host "ok: $destino\$vol.tar.gz"
}
