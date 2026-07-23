@echo off
REM ============================================
REM 一键构建 + 启动全部 Spring Cloud 服务
REM 依赖：Nacos / MySQL / Redis 已就绪
REM   - Nacos：双击 nacos-start.bat 启动
REM 日志: deploy\logs\*.log   停止: deploy\stop-all.bat
REM ============================================
title 在线考试系统 - 服务启动器

setlocal enabledelayedexpansion
set ROOT=%~dp0..
set VER=0.0.1-SNAPSHOT
set LOGDIR=%ROOT%\deploy\logs
if not exist "%LOGDIR%" mkdir "%LOGDIR%"

echo ================================================
echo  在线考试系统 Spring Cloud 一键启动
echo ================================================

REM ---------- 检查 Nacos 是否在线 ----------
echo.
echo [检查] Nacos 连通性 ...
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri 'http://127.0.0.1:8848/nacos/' -TimeoutSec 3 -UseBasicParsing; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    echo [警告] Nacos 未响应 http://127.0.0.1:8848/nacos
    echo        请先双击 deploy\nacos-start.bat 启动 Nacos，等约 20 秒后再运行本脚本。
    echo.
    choice /c YN /m "仍要强行启动考试服务吗?(Y=是/N=否)"
    if errorlevel 2 goto :eof
)

REM ---------- 构建全部可执行 jar（跳过测试） ----------
echo.
echo [构建] 编译打包全部服务...
cd /d "%ROOT%"
call gradlew.bat bootJar -x test --no-daemon
if errorlevel 1 (
    echo 构建失败，请检查错误日志
    pause
    exit /b 1
)

REM ---------- 启动业务服务 ----------
echo.
echo [启动] 依次启动服务...

call :start exam-user-service      8081 "用户服务"
call :start exam-question-service  8082 "题目服务"
call :start exam-paper-service     8083 "试卷服务"
call :start exam-exam-service      8084 "考试服务"
call :start exam-score-service     8085 "成绩服务"
call :start exam-wrong-service     8086 "错题服务"
call :start exam-analysis-service  8087 "分析服务"

echo 等待业务服务注册到 Nacos（约 15 秒）...
timeout /t 15 /nobreak >nul

call :start exam-gateway           8080 "网关"

echo.
echo ================================================
echo  全部启动完毕！
echo  健康检查: http://localhost:8080/test
echo  前端入口: http://localhost:8080
echo  日志目录: deploy\logs\
echo  关闭所有: deploy\stop-all.bat
echo ================================================
echo.
pause
goto :eof

REM ---------- 启动单个服务 ----------
:start
set MODULE=%1
set PORT=%2
set NAME=%3
set JAR=%ROOT%\%MODULE%\build\libs\%MODULE%-%VER%.jar

if not exist "%JAR%" (
    echo [错误] 缺少 %JAR%
    goto :eof
)
echo    [%PORT%] !NAME! ...
start "!NAME!-!PORT!" /min cmd /c java -jar "%JAR%" ^> "%LOGDIR%\%MODULE%.log" 2^>^&1
goto :eof
