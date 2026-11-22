# Valida a stack antes de subir ou de commitar: compose, JSONs dos painéis e variáveis.
# Uso: .\scripts\validar.ps1
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
$erros = 0

Write-Host "1) docker compose config"
$temEnv = Test-Path ".env"
if (-not $temEnv) { Copy-Item ".env.example" ".env" }
try { docker compose config -q; Write-Host "   ok" } catch { Write-Host "   FALHOU"; $erros++ }
if (-not $temEnv) { Remove-Item ".env" }

Write-Host "2) JSON dos painéis do Grafana"
Get-ChildItem "grafana/dashboards/*.json" | ForEach-Object {
  try { Get-Content $_.FullName -Raw | ConvertFrom-Json | Out-Null; Write-Host "   ok  $($_.Name)" }
  catch { Write-Host "   FALHOU  $($_.Name)"; $erros++ }
}

Write-Host "3) Arquivos obrigatórios"
foreach ($f in "jenkins/casc/jenkins.yaml", "jenkins/Dockerfile", "prometheus/prometheus.yml", "loki/loki-config.yaml", "alloy/config.alloy") {
  if (Test-Path $f) { Write-Host "   ok  $f" } else { Write-Host "   FALTA  $f"; $erros++ }
}

if ($erros -gt 0) { Write-Host "`n$erros problema(s) encontrado(s)"; exit 1 }
Write-Host "`nTudo certo."
