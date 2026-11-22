#!/usr/bin/env bash
# Valida a stack antes de subir ou de commitar: compose, JSON e YAML.
# Uso: ./scripts/validar.sh
set -euo pipefail
cd "$(dirname "$0")/.."
tinha_env=true; [ -f .env ] || { tinha_env=false; cp .env.example .env; }
trap '$tinha_env || rm -f .env' EXIT
echo "1) docker compose config"; docker compose config -q && echo "   ok"
echo "2) JSON e YAML"
python3 - <<'PY'
import glob, json, sys
try:
    import yaml
except ImportError:
    yaml = None
erros = 0
for f in glob.glob("grafana/dashboards/*.json"):
    try: json.load(open(f, encoding="utf-8")); print("   ok ", f)
    except Exception as e: print("   FALHOU", f, e); erros += 1
if yaml:
    for f in glob.glob("**/*.y*ml", recursive=True):
        if f.startswith(".github"): continue
        try: yaml.safe_load(open(f, encoding="utf-8").read().replace("${ALERT_WEBHOOK_URL}", "x")); print("   ok ", f)
        except Exception as e: print("   FALHOU", f, e); erros += 1
else:
    print("   (PyYAML ausente: YAML nao verificado)")
sys.exit(1 if erros else 0)
PY
echo "Tudo certo."
