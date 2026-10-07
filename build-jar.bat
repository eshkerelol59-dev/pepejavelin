@echo off
title Javelin Client - build
cd /d "%~dp0"

echo ============================================
echo   Javelin Client - build jar
echo ============================================
echo.

call gradlew.bat build --console=plain
set EXITCODE=%ERRORLEVEL%

echo.
if %EXITCODE% NEQ 0 (
    echo [ERROR] Build failed - see log above.
) else (
    echo Done! Jar files are in build\libs\
)
echo.
echo Latest jar:
for /f "delims=" %%F in ('dir /b /o-d build\libs\*.jar 2^>nul ^| findstr /v "sources"') do (
    echo   build\libs\%%F
    goto :done
)
:done
pause
