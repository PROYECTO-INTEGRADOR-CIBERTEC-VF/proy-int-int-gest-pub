param(
    [string]$BackendOpenApiUrl = "http://localhost:8080/v3/api-docs",
    [string]$SpecOutput = "openapi/saip-openapi.json"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$specDir = Split-Path -Parent $SpecOutput
New-Item -ItemType Directory -Path $specDir -Force | Out-Null

Write-Host "Descargando contrato OpenAPI desde $BackendOpenApiUrl"
Invoke-WebRequest -Uri $BackendOpenApiUrl -UseBasicParsing -OutFile $SpecOutput

Set-Location "$repoRoot/transparencia-frontend"
Write-Host "Generando tipos TypeScript desde $SpecOutput"
npm run openapi:generate

Write-Host "OpenAPI sincronizado en transparencia-frontend/src/app/api/schema.ts"
