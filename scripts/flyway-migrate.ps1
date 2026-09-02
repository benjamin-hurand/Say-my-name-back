param(
    [ValidateSet("local", "dev", "prod")]
    [string]$Environment = "local"
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "flyway-common.ps1")

if ($Environment -eq "prod") {
    throw @"
Production migrations are not allowed from scripts/flyway-migrate.ps1.

Production migrations must be executed through the dedicated CI/CD migration workflow.
"@
}

$url = Assert-EnvironmentVariable "FLYWAY_URL"
$username = Assert-EnvironmentVariable "FLYWAY_USERNAME"
$password = Assert-EnvironmentVariable "FLYWAY_PASSWORD"

Write-Host "[INFO] Environment: $Environment"

Invoke-FlywayMaven `
    -Command "migrate" `
    -Url $url `
    -Username $username `
    -Password $password