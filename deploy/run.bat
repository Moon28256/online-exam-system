@echo off
REM ============================================================
REM  Online Exam System - Run Script (Windows)
REM  Starts backend jar (serves frontend + API on port 8080).
REM  Override DB connection via the env vars below.
REM ============================================================
cd /d "%~dp0.."

REM --- Database connection (edit for deployment to other machines) ---
REM NOTE: & inside the quoted set is literal (no ^ escape needed).
set "DB_URL=jdbc:mysql://localhost:3306/exam_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"
set "DB_USER=root"
set "DB_PASSWORD=mzls6666mo"

echo Starting Online Exam System (port 8080)...
echo URL: http://localhost:8080

set "JAR=exam-backend\exam-backend\build\libs\exam-backend-0.0.1-SNAPSHOT.jar"
if not exist "%JAR%" (
    echo [ERROR] jar not found. Run deploy\build.bat first.
    pause
    exit /b 1
)

java -jar "%JAR%"
