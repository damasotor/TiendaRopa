<#
  run-and-check-mongo.ps1
  Usage:
    # 1) Use the SPRING_DATA_MONGODB_URI already exported in the shell:
    .\scripts\run-and-check-mongo.ps1

    # 2) Or pass a URI as argument (temporary for the session):
    .\scripts\run-and-check-mongo.ps1 -Uri "mongodb+srv://user:pass@cluster0.../ecommerceDB"

  What it does:
  - Shows current SPRING_DATA_MONGODB_URI (if any)
  - If a URI argument is provided, it sets that value for the current session
  - Starts `mvn spring-boot:run` and redirects stdout/stderr to ./logs/app.log
  - Tails the log file and stops when it sees the line printed by the app that indicates
    the connected DB ("Conectado a MongoDB - base de datos: <name>")
  - Leaves the mvn process running; press Ctrl+C in this script to stop tailing, or use
    the printed PID to terminate the mvn process if you want to stop it.
#>

param(
    [string] $Uri
)

# Ensure logs directory exists
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$logsDir = Join-Path $scriptDir "..\logs" | Resolve-Path -Relative -ErrorAction SilentlyContinue
if (-not (Test-Path "$scriptDir\..\logs")) {
    New-Item -ItemType Directory -Path "$scriptDir\..\logs" | Out-Null
}
$logFile = Join-Path $scriptDir "..\logs\app.log"
if (Test-Path $logFile) { Remove-Item $logFile -Force }

Write-Host "-- run-and-check-mongo.ps1 --" -ForegroundColor Cyan

if ($Uri) {
    Write-Host "Setting SPRING_DATA_MONGODB_URI for this session to:" -ForegroundColor Yellow
    Write-Host "  $Uri" -ForegroundColor Gray
    $env:SPRING_DATA_MONGODB_URI = $Uri
} else {
    if ($env:SPRING_DATA_MONGODB_URI) {
        Write-Host "SPRING_DATA_MONGODB_URI is set in this session:" -ForegroundColor Green
        Write-Host "  $($env:SPRING_DATA_MONGODB_URI)" -ForegroundColor Gray
    } else {
        Write-Host "SPRING_DATA_MONGODB_URI is NOT set in this session." -ForegroundColor Red
        Write-Host "The script will still run and use application.properties fallback if any." -ForegroundColor Yellow
    }
}

# Start mvn in background and redirect output
Push-Location $scriptDir\..\
Write-Host "Starting 'mvn spring-boot:run' in: $PWD" -ForegroundColor Cyan

$errFile = "$logFile.err"
$job = $null
try {
    # Use separate files for stdout and stderr (Start-Process disallows using the same path for both)
    $startInfo = Start-Process -FilePath mvn -ArgumentList 'spring-boot:run' -RedirectStandardOutput $logFile -RedirectStandardError $errFile -NoNewWindow -PassThru
    Write-Host "Maven started with PID: $($startInfo.Id)" -ForegroundColor Green
} catch {
    Write-Host "Warning: Start-Process with redirected streams failed: $_" -ForegroundColor Yellow
    Write-Host "Falling back to Start-Job to run mvn and capture output to log files." -ForegroundColor Yellow
    # Start as a background job that redirects both stdout and stderr to files
    $job = Start-Job -ScriptBlock { param($lf,$ef) mvn spring-boot:run *> $lf 2>&1 } -ArgumentList $logFile, $errFile
    Write-Host "Maven started as background job with Id: $($job.Id)" -ForegroundColor Green
    $startInfo = $null
}

# Tail the log and scan for DB message
Write-Host "Tailing log (file: $logFile). Waiting for DB connection message..." -ForegroundColor Cyan

try {
    # We can wait for multiple markers: DB connection message and optionally DataLoader message.
    $waitForDataLoader = $true # default: also look for DataLoader message
    $dataLoaderPattern = 'Productos creados'

    $dbDetected = $false
    $dataLoaderDetected = $false

    # Monitor both stdout and stderr files so we capture all output. If Start-Process wrote only to $logFile
    # then $errFile may be empty but Get-Content accepts multiple paths.
    Get-Content -Path $logFile,$errFile -Wait -Tail 0 | ForEach-Object {
        $line = $_
        Write-Host $line

        if (-not $dbDetected -and $line -match "Conectado a MongoDB - base de datos:\s*(.+)") {
            $dbName = $Matches[1]
            Write-Host "\n>>> Detected DB: $dbName" -ForegroundColor Green
            $dbDetected = $true
        }

        if ($waitForDataLoader -and -not $dataLoaderDetected -and $line -match $dataLoaderPattern) {
            Write-Host "\n>>> Detected DataLoader message: $dataLoaderPattern" -ForegroundColor Green
            $dataLoaderDetected = $true
        }

        if ($dbDetected -and ($dataLoaderDetected -or -not $waitForDataLoader)) {
            $pidText = if ($startInfo) { $startInfo.Id } elseif ($job) { "job:$($job.Id)" } else { "unknown" }
            Write-Host "Maven PID/Job: $pidText. Application is running. Press Ctrl+C to stop tailing (Maven/job keeps running)." -ForegroundColor Yellow
            break
        }
    }
} catch {
    Write-Host "Error while tailing log: $_" -ForegroundColor Red
}

$pidText = if ($startInfo) { $startInfo.Id } elseif ($job) { "job:$($job.Id)" } else { "" }
if ($pidText -ne "") {
    Write-Host "Script finished (tail loop ended). If you want to stop the app, use:`n    Stop-Process -Id $pidText -Force" -ForegroundColor Cyan
} else {
    Write-Host "Script finished (tail loop ended). If you want to stop the app, locate the mvn process manually or use the job Id shown earlier." -ForegroundColor Cyan
}
Pop-Location

# End of script
