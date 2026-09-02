param(
    [string]$Query
)

$ErrorActionPreference = "Stop"

$backendRoot = Split-Path -Parent $PSScriptRoot
$secretsFile = Join-Path $backendRoot ".secrets.local.ps1"

if (Test-Path $secretsFile) {
    . $secretsFile
}

function Assert-EnvironmentVariable {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $value = [Environment]::GetEnvironmentVariable($Name)

    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Variable d'environnement requise absente ou vide : $Name"
    }

    return $value
}

function Parse-JdbcMySqlUrl {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url
    )

    if ($Url -notmatch '^jdbc:mysql://([^/:]+)(?::(\d+))?/([^?]+)') {
        throw "Format JDBC MySQL non supporte : $Url"
    }

    return @{
        Host     = $Matches[1]
        Port     = if ($Matches[2]) { $Matches[2] } else { "3306" }
        Database = $Matches[3]
    }
}

$url = Assert-EnvironmentVariable "DB_URL"
$username = Assert-EnvironmentVariable "AGENT_DB_USERNAME"
$password = Assert-EnvironmentVariable "AGENT_DB_PASSWORD"

$connection = Parse-JdbcMySqlUrl $url

Write-Host "[INFO] Database: $($connection.Database)"
Write-Host "[INFO] Host: $($connection.Host):$($connection.Port)"
Write-Host "[INFO] User: $username"

$tempDefaultsFile = Join-Path `
    ([System.IO.Path]::GetTempPath()) `
    ("saymyname-mysql-" + [System.Guid]::NewGuid().ToString("N") + ".cnf")

try {
    @"
[client]
host=$($connection.Host)
port=$($connection.Port)
user=$username
password=$password
"@ | Set-Content -Path $tempDefaultsFile -Encoding ASCII

    $mysqlArgs = @(
        "--defaults-extra-file=$tempDefaultsFile",
        $connection.Database
    )

    if ($Query) {
        & mysql @mysqlArgs "--execute=$Query"
    }
    else {
        & mysql @mysqlArgs
    }

    if ($LASTEXITCODE -ne 0) {
        throw "MySQL command failed (exit code $LASTEXITCODE)"
    }
}
finally {
    if (Test-Path $tempDefaultsFile) {
        Remove-Item $tempDefaultsFile -Force
    }
}