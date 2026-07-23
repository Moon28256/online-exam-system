#requires -Version 5.0
# ============================================================
#  在线考试系统（Spring Cloud）— 一键公网隧道
#  依赖：服务已通过 run-all.bat 启动（或手动启动了网关 :8080）
#  按 Ctrl+C 停止隧道
# ============================================================
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

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
    Write-Host '[ERROR] cpolar 未找到。' -ForegroundColor Red
    Write-Host '  1) 从 https://www.cpolar.com/download 下载安装'
    Write-Host '  2) 首次使用执行:  cpolar authtoken <你的token>'
    Write-Host ''
    Read-Host '按 Enter 退出'
    exit 1
}

# --- check gateway is up ---
$gatewayUp = $false
try {
    $r = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing -TimeoutSec 3
    if ($r.StatusCode -eq 200) { $gatewayUp = $true }
} catch {}

if (-not $gatewayUp) {
    Write-Host '[提示] 网关 :8080 未响应。' -ForegroundColor Yellow
    Write-Host '  请先在另一个终端执行:  deploy\run-all.bat'
    Write-Host '  启动全部服务后再运行本脚本。'
    Write-Host ''
    $answer = Read-Host '是否现在调用 run-all.bat 启动服务？(y/n)'
    if ($answer -eq 'y' -or $answer -eq 'Y') {
        $runAll = Join-Path (Split-Path -Parent $PSScriptRoot) 'deploy\run-all.bat'
        if (Test-Path $runAll) {
            Write-Host '正在启动全部服务（新窗口）...' -ForegroundColor Cyan
            Start-Process -FilePath 'cmd' -ArgumentList "/c `"$runAll`"" -WindowStyle Normal
            Write-Host '等待服务就绪（约 30 秒）...' -ForegroundColor Cyan
            $ready = $false
            for ($i = 0; $i -lt 60; $i++) {
                Start-Sleep -Seconds 1
                try {
                    $r = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing -TimeoutSec 3
                    if ($r.StatusCode -eq 200) { $ready = $true; break }
                } catch {}
            }
            if (-not $ready) {
                Write-Host '[ERROR] 服务启动超时，请手动检查。' -ForegroundColor Red
                Read-Host '按 Enter 退出'
                exit 1
            }
            Write-Host '服务就绪: http://localhost:8080' -ForegroundColor Green
        }
    } else {
        Write-Host '请先启动服务再运行本脚本。' -ForegroundColor Yellow
        Read-Host '按 Enter 退出'
        exit 1
    }
} else {
    Write-Host '网关已就绪: http://localhost:8080（复用）' -ForegroundColor DarkYellow
}

# --- start cpolar tunnel ---
Write-Host '正在启动 cpolar 隧道...' -ForegroundColor Cyan
$logFile = [IO.Path]::GetTempFileName()
$cpolar = Start-Process -FilePath $cpolarExe `
    -ArgumentList @('http','8080','-log','stdout','-log-level','INFO') `
    -PassThru -RedirectStandardOutput $logFile -WindowStyle Minimized

# --- parse public URL from cpolar log ---
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
    Write-Host '  公网地址:' -ForegroundColor Green
    Write-Host '  ' $url -ForegroundColor White
    Write-Host '============================================================' -ForegroundColor Green
    Write-Host '把这个地址发给别人即可访问。按 Ctrl+C 停止后地址失效。' -ForegroundColor Green
} else {
    Write-Host '未能自动读取隧道地址。' -ForegroundColor Yellow
    Write-Host '  打开 cpolar 仪表盘查看 "转发" 下的 https://... 地址。' -ForegroundColor Yellow
    Write-Host '  http://localhost:9200' -ForegroundColor Yellow
}

Write-Host ''
Write-Host '服务运行中。按 Ctrl+C 停止。' -ForegroundColor Cyan
Write-Host ''

# --- keep alive ---
try {
    while ($true) {
        if ($cpolar -and $cpolar.HasExited) {
            Write-Host 'cpolar 已退出。' -ForegroundColor DarkYellow
            break
        }
        Start-Sleep -Seconds 1
    }
} finally {
    Write-Host ''
    Write-Host '正在停止 cpolar 隧道...' -ForegroundColor Cyan
    Stop-Pids @($cpolar)
    if ($logFile) { try { Remove-Item $logFile -Force -ErrorAction SilentlyContinue } catch {} }
    Write-Host '已停止。Bye.' -ForegroundColor Green
}
