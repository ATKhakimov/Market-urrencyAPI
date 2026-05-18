param(
    [string]$JdkHome = "C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $JdkHome)) {
    Write-Error "JDK path not found: $JdkHome"
    exit 1
}

$env:JAVA_HOME = $JdkHome
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$projects = @("currency-rate-provider", "rate-printer")

foreach ($project in $projects) {
    $projectPath = Join-Path $root $project
    if (-not (Test-Path $projectPath)) {
        Write-Error "Project path not found: $projectPath"
        exit 1
    }

    Write-Host "Running tests in $project with JAVA_HOME=$env:JAVA_HOME"
    Push-Location $projectPath
    try {
        .\gradlew.bat test
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Tests failed in $project"
            exit $LASTEXITCODE
        }
    }
    finally {
        Pop-Location
    }
}

Write-Host "All tests passed for both modules using Java 25."
