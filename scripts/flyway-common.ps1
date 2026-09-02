$ErrorActionPreference = "Stop"

# Root du backend, indépendamment du répertoire depuis lequel le script est lancé.
$script:BackendRoot = Split-Path -Parent $PSScriptRoot

$script:WebappPom = Join-Path $script:BackendRoot "webapp\pom.xml"
$script:FlywayMigrationsPath = Join-Path `
    $script:BackendRoot `
    "webapp\src\main\resources\db\migration"

$secretsFile = Join-Path $script:BackendRoot ".secrets.local.ps1"

# En local, charge les secrets s'ils existent.
#
# En CI/CD, ce fichier n'existera pas :
# les variables seront fournies directement par l'environnement.
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

function Invoke-FlywayMaven {
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet("validate", "migrate")]
        [string]$Command,

        [Parameter(Mandatory = $true)]
        [string]$Url,

        [Parameter(Mandatory = $true)]
        [string]$Username,

        [Parameter(Mandatory = $true)]
        [string]$Password
    )

    Write-Host "[INFO] Flyway command: $Command"
    Write-Host "[INFO] Database: $Url"
    Write-Host "[INFO] User: $Username"
    Write-Host "[INFO] Migrations: $script:FlywayMigrationsPath"

    & mvn `
        -f $script:WebappPom `
        "flyway:$Command" `
        "-Dflyway.url=$Url" `
        "-Dflyway.user=$Username" `
        "-Dflyway.password=$Password" `
        "-Dflyway.locations=filesystem:$script:FlywayMigrationsPath"

    if ($LASTEXITCODE -ne 0) {
        throw "Flyway $Command failed (exit code $LASTEXITCODE)"
    }

    Write-Host "[INFO] Flyway $Command successful."
}