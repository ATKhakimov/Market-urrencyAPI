Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$brokerUrl = if ($env:PACT_BROKER_BASE_URL) { $env:PACT_BROKER_BASE_URL } else { 'http://localhost:9292' }

Write-Host "Starting infrastructure (zookeeper + pact broker + db)..."
Push-Location $root
try {
	docker compose up -d zookeeper pact-broker-db pact-broker
} finally {
	Pop-Location
}

Write-Host "Publishing consumer pact from rate-printer..."
Push-Location (Join-Path $root 'rate-printer')
try {
	$env:PACT_BROKER_BASE_URL = $brokerUrl
	if (-not $env:PACT_CONSUMER_VERSION) {
		$env:PACT_CONSUMER_VERSION = 'local-dev'
	}
	if (-not $env:PACT_CONSUMER_TAG) {
		$env:PACT_CONSUMER_TAG = 'dev'
	}
	.\gradlew.bat test publishPactsToBroker --no-daemon
} finally {
	Pop-Location
}

Write-Host "Verifying provider against broker contracts..."
Push-Location (Join-Path $root 'currency-rate-provider')
try {
	$env:PACT_BROKER_BASE_URL = $brokerUrl
	.\gradlew.bat build --no-daemon
} finally {
	Pop-Location
}

Write-Host "Done. Pact e2e flow completed successfully."
Write-Host "Broker URL: $brokerUrl"
