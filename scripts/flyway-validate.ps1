$ErrorActionPreference = "Stop"

$backendRoot = Split-Path -Parent $PSScriptRoot
$secretsFile = Join-Path $backendRoot ".secrets.local.ps1"
$webappPom = Join-Path $backendRoot "webapp\pom.xml"
$migrationsPath = Join-Path $backendRoot "webapp\src\main\resources\db\migration"

if (-not (Test-Path $secretsFile)) {
    throw "Fichier de secrets introuvable : $secretsFile"
}

# Charge les variables $env:... définies dans le fichier local
. $secretsFile

$requiredVariables = @(
    "FLYWAY_URL",
    "AGENT_DB_USERNAME",
    "AGENT_DB_PASSWORD"
)

foreach ($variableName in $requiredVariables) {
    $value = [Environment]::GetEnvironmentVariable($variableName)

    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Variable d'environnement requise absente ou vide : $variableName"
    }
}

Write-Host "[INFO] Flyway validation"
Write-Host "[INFO] Database: $env:FLYWAY_URL"
Write-Host "[INFO] User: $env:AGENT_DB_USERNAME"
Write-Host "[INFO] Migrations: $migrationsPath"

mvn `
    -f $webappPom `
    "flyway:validate" `
    "-Dflyway.url=$env:FLYWAY_URL" `
    "-Dflyway.user=$env:AGENT_DB_USERNAME" `
    "-Dflyway.password=$env:AGENT_DB_PASSWORD" `
    "-Dflyway.locations=filesystem:$migrationsPath"

if ($LASTEXITCODE -ne 0) {
    throw "Flyway validation failed (exit code $LASTEXITCODE)"
}

Write-Host "[INFO] Flyway validation successful."