@echo off
REM ============================================================
REM  Online Exam System - Build Script (Windows)
REM  Step: build frontend -> copy dist to backend static -> build jar
REM ============================================================
cd /d "%~dp0.."

echo.
echo [1/3] Build frontend (npm run build)...
cd exam-frontend
call npm run build
if errorlevel 1 (
    echo [ERROR] Frontend build failed. Check Node.js / npm.
    pause
    exit /b 1
)
cd ..

echo.
echo [2/3] Copy frontend dist to backend static dir...
set "STATIC_DIR=exam-backend\exam-backend\src\main\resources\static"
if not exist "%STATIC_DIR%" mkdir "%STATIC_DIR%"
del /q /f "%STATIC_DIR%\*" 2>nul
for /d %%i in ("%STATIC_DIR%\*") do rmdir /s /q "%%i" 2>nul
xcopy /e /y /i "exam-frontend\dist\*" "%STATIC_DIR%\" >nul
if errorlevel 1 (
    echo [ERROR] Copy frontend dist failed.
    pause
    exit /b 1
)
echo     Copied to %STATIC_DIR%

echo.
echo [3/3] Build backend jar (gradlew build -x test)...
cd exam-backend\exam-backend
call .\gradlew.bat build -x test
if errorlevel 1 (
    echo [ERROR] Backend build failed.
    pause
    exit /b 1
)
cd ..\..

echo.
echo ============================================================
echo  Build done.
echo  jar: exam-backend\exam-backend\build\libs\exam-backend-0.0.1-SNAPSHOT.jar
echo  Run with: deploy\run.bat
echo ============================================================
