@echo off
title Javelin Client
cd /d "%~dp0"

echo ============================================
echo   Javelin Client - runClient (dev build)
echo ============================================
echo.

call gradlew.bat runClient --console=plain
set EXITCODE=%ERRORLEVEL%

echo.
if %EXITCODE% NEQ 0 (
    echo [ERROR] Client exited with code %EXITCODE% - see log above.
) else (
    echo Client closed.
)
pause
