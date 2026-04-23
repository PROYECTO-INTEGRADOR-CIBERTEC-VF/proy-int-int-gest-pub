#!/usr/bin/env bash
set -euo pipefail

BACKEND_OPENAPI_URL="${1:-http://localhost:8080/v3/api-docs}"
SPEC_OUTPUT="openapi/saip-openapi.json"

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

mkdir -p "$(dirname "$SPEC_OUTPUT")"

echo "Descargando contrato OpenAPI desde $BACKEND_OPENAPI_URL"
curl -fsSL "$BACKEND_OPENAPI_URL" -o "$SPEC_OUTPUT"

echo "Generando tipos TypeScript desde $SPEC_OUTPUT"
(
  cd transparencia-frontend
  npm run openapi:generate
)

echo "OpenAPI sincronizado en transparencia-frontend/src/app/api/schema.ts"
