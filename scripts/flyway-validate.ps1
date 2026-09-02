$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "flyway-common.ps1")

$url = Assert-EnvironmentVariable "FLYWAY_URL"
$username = Assert-EnvironmentVariable "AGENT_DB_USERNAME"
$password = Assert-EnvironmentVariable "AGENT_DB_PASSWORD"

Invoke-FlywayMaven `
    -Command "validate" `
    -Url $url `
    -Username $username `
    -Password $password