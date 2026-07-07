@echo off
REM ============================================================
REM  Online Exam System - one-click public URL (double-click me)
REM  Starts backend + cpolar tunnel and prints the public URL.
REM  Press Ctrl+C in the window to stop.
REM ============================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0online.ps1"
