#requires -Version 5.0
# ============================================================
#  Online Exam System - one-click public tunnel
#  Starts backend (port 8080) + cpolar tunnel, prints the public URL.
#  The URL stays valid until you press Ctrl+C in this window.
# ============================================================
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$JAR = Join-Path $root 'exam-backend\exam-backend\build\libs\exam-backend-0.0.1-SNAPSHOT.jar'

function Stop-Pids($procs) {
    foreach ($p in $procs) {
        if ($p -and -not $p.HasExited) {
            try { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue } catch {}
        }
    }
}

# --- locate cpolar (PATH first, then common install dirs) ---
$cpolarExe = $null
$cmd = Get-Command cpolar -ErrorAction SilentlyContinue
if ($cmd) { $cpolarExe = $cmd.Source }
if (-not $cpolarExe) {
    foreach ($cand in @(
        "$env:ProgramFiles\cpolar\cpolar.exe",
        "${env:ProgramFiles(x86)}\cpolar\cpolar.exe",
        "$env:LOCALAPPDATA\cpolar\cpolar.exe"
    )) {
        if (Test-Path $cand) { $cpolarExe = $cand; break }
    }
}

# --- pre-flight checks ---
if (-not $cpolarExe) {
    Write-Host '[ERROR] cpolar not found.' -ForegroundColor Red
    Write-Host '  1) Install from https://www.cpolar.com/download'
    Write-Host '  2) Run once:  cpolar authtoken <your-token>'
    Read-Host 'Press Enter to exit'
    exit 1
}
if (-not (Test-Path $JAR)) {
    Write-Host '[ERROR] jar not found.' -ForegroundColor Red
    Write-Host '  Run deploy\build.bat first to build the project.'
    Read-Host 'Press Enter to exit'
    exit 1
}

# --- DB connection (edit here for deployment to other machines) ---
$env:DB_URL     = 'jdbc:mysql://localhost:3306/exam_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8'
$env:DB_USER    = 'root'
$env:DB_PASSWORD = 'mzls6666mo'

$backend = $null
$cpolar  = $null
$logFile = $null
$startedBackend = $false

try {
    # --- 1. backend (skip if already running on 8080) ---
    $alreadyUp = $false
    try {
        $r = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing -TimeoutSec 2
        if ($r.StatusCode -eq 200) { $alreadyUp = $true }
    } catch {}

    if ($alreadyUp) {
        Write-Host 'Backend already running on :8080 (reusing).' -ForegroundColor DarkYellow
    } else {
        Write-Host 'Starting backend (port 8080)...' -ForegroundColor Cyan
        $backend = Start-Process -FilePath 'java' -ArgumentList @('-jar', "`"$JAR`"") -PassThru -WindowStyle Minimized
        $startedBackend = $true
        $ready = $false
        for ($i = 0; $i -lt 60; $i++) {
            Start-Sleep -Seconds 1
            try {
                $r = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing -TimeoutSec 2
                if ($r.StatusCode -eq 200) { $ready = $true; break }
            } catch {}
        }
        if (-not $ready) {
            Write-Host '[ERROR] Backend did not become ready.' -ForegroundColor Red
            Write-Host '  Check: MySQL is running, DB_PASSWORD matches, port 8080 is free.' -ForegroundColor Red
            throw 'backend not ready'
        }
        Write-Host 'Backend ready: http://localhost:8080' -ForegroundColor Green
    }

    # --- 2. cpolar tunnel (log to stdout so we can parse the URL) ---
    Write-Host 'Starting cpolar tunnel...' -ForegroundColor Cyan
    $logFile = [IO.Path]::GetTempFileName()
    $cpolar = Start-Process -FilePath $cpolarExe `
        -ArgumentList @('http','8080','-log','stdout','-log-level','INFO') `
        -PassThru -RedirectStandardOutput $logFile -WindowStyle Minimized

    # --- 3. parse the public URL from cpolar's log ---
    $url = $null
    $pattern = 'Tunnel established at (https://[A-Za-z0-9.-]+)'
    for ($i = 0; $i -lt 40; $i++) {
        Start-Sleep -Seconds 1
        if ($cpolar.HasExited) { break }
        if (Test-Path $logFile) {
            $m = Select-String -Path $logFile -Pattern $pattern -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($m) { $url = $m.Matches[0].Groups[1].Value; break }
        }
    }

    Write-Host ''
    if ($url) {
        Write-Host '============================================================' -ForegroundColor Green
        Write-Host '  PUBLIC URL:' -ForegroundColor Green
        Write-Host '  ' $url -ForegroundColor White
        Write-Host '============================================================' -ForegroundColor Green
        Write-Host 'Send this URL to others. Valid until you stop this script.' -ForegroundColor Green
    } else {
        Write-Host 'Could not auto-read the tunnel URL.' -ForegroundColor Yellow
        Write-Host '  Open the cpolar dashboard in your browser to get it:' -ForegroundColor Yellow
        Write-Host '  (the "Forwarding" https://... address is your public URL)' -ForegroundColor Yellow
    }

    Write-Host ''
    Write-Host 'Service is running. Press Ctrl+C in this window to stop.' -ForegroundColor Cyan
    Write-Host ''

    # --- 4. keep alive until cpolar/backend exits or user hits Ctrl+C ---
    while ($true) {
        if ($cpolar -and $cpolar.HasExited) { Write-Host 'cpolar exited.' -ForegroundColor DarkYellow; break }
        if ($startedBackend -and $backend.HasExited) { Write-Host 'backend exited.' -ForegroundColor DarkYellow; break }
        Start-Sleep -Seconds 1
    }
}
finally {
    Write-Host ''
    Write-Host 'Stopping services...' -ForegroundColor Cyan
    Stop-Pids @($cpolar)
    if ($startedBackend) { Stop-Pids @($backend) }
    if ($logFile) { try { Remove-Item $logFile -Force -ErrorAction SilentlyContinue } catch {} }
    Write-Host 'Stopped. Bye.' -ForegroundColor Green
}
