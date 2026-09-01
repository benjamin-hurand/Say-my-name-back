param(
    [string]$Query
)

$ErrorActionPreference = "Stop"

if (-not $env:AGENT_DB_USERNAME) {
    throw "AGENT_DB_USERNAME is not defined."
}

if (-not $env:AGENT_DB_PASSWORD) {
    throw "AGENT_DB_PASSWORD is not defined."
}

$database = "saymyname"
$hostName = "localhost"

if ($Query) {
    mysql `
        --host=$hostName `
        --user=$env:AGENT_DB_USERNAME `
        "--password=$env:AGENT_DB_PASSWORD" `
        $database `
        --execute="$Query"
}
else {
    mysql `
        --host=$hostName `
        --user=$env:AGENT_DB_USERNAME `
        "--password=$env:AGENT_DB_PASSWORD" `
        $database
}