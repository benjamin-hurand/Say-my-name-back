$ErrorActionPreference = "Stop"

if (-not $env:FLYWAY_USERNAME) {
    throw "FLYWAY_USERNAME is not defined."
}

if (-not $env:FLYWAY_PASSWORD) {
    throw "FLYWAY_PASSWORD is not defined."
}

$url = if ($env:FLYWAY_URL) {
    $env:FLYWAY_URL
} else {
    "jdbc:mysql://localhost:3306/saymyname"
}

mvn -f .\webapp\pom.xml `
    flyway:migrate `
    "-Dflyway.url=$url" `
    "-Dflyway.user=$env:FLYWAY_USERNAME" `
    "-Dflyway.password=$env:FLYWAY_PASSWORD" `
    "-Dflyway.locations=filesystem:webapp/src/main/resources/db/migration"