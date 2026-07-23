@echo off
REM ============================================
REM Nacos 启动器（standalone 模式）
REM 默认路径 D:\nacos\nacos；可用 NACOS_HOME 环境变量覆盖
REM ============================================
title Nacos 启动器

set "NACOS_HOME=%NACOS_HOME%"
if "%NACOS_HOME%"=="" set "NACOS_HOME=D:\nacos\nacos"

if not exist "%NACOS_HOME%\bin\startup.cmd" (
    echo [错误] 未找到 %NACOS_HOME%\bin\startup.cmd
    echo        请把 Nacos 解压到 D:\nacos，或设置 NACOS_HOME 环境变量指向你的 Nacos 目录
    echo        （目录下需含 bin\startup.cmd）
    pause
    exit /b 1
)

echo ================================================
echo  启动 Nacos standalone 模式
echo  安装目录: %NACOS_HOME%
echo  控制台:   http://127.0.0.1:8848/nacos  （账号 / 密码均为 nacos）
echo  约需 20 秒，日志出现 "Nacos started successfully" 即启动完成
echo ================================================
echo.

start "Nacos" "%NACOS_HOME%\bin\startup.cmd" -m standalone

echo Nacos 已在新窗口启动，请勿关闭该窗口。
echo 等待约 20 秒后，再运行 run-all.bat 启动考试系统。
timeout /t 5 >nul
