@echo off
REM ============================================
REM 一键停止全部 Spring Cloud 服务 + Nacos（按端口杀进程）
REM 双击即停止 8 个考试服务（8080-8087）和 Nacos（8848）
REM ============================================
title 在线考试系统 - 服务停止器

setlocal enabledelayedexpansion
set LOGDIR=%~dp0logs

echo ================================================
echo  在线考试系统 Spring Cloud 一键停止（含 Nacos）
echo ================================================
echo.

REM 逐端口查找并杀进程
call :stop 8080 "网关"
call :stop 8081 "用户服务"
call :stop 8082 "题目服务"
call :stop 8083 "试卷服务"
call :stop 8084 "考试服务"
call :stop 8085 "成绩服务"
call :stop 8086 "错题服务"
call :stop 8087 "分析服务"
call :stop 8848 "Nacos"

REM 清理日志目录的 PID 文件（若存在）
del /q "%LOGDIR%\*.pid" 2>nul

echo.
echo 全部已停止（含 Nacos）。
echo 下次启动请先双击 nacos-start.bat 启动 Nacos，再运行 run-all.bat。
echo.
pause
goto :eof

:stop
set PORT=%1
set NAME=%2
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
    set PID=%%a
    echo 停止 !NAME! :!PORT!  PID=!PID!
    taskkill /PID !PID! /F >nul 2>&1
)
goto :eof
