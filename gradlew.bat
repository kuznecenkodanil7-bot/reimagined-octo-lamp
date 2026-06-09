@echo off
setlocal
set APP_HOME=%~dp0
set GRADLE_VERSION=9.4.1
set DIST_ROOT=%APP_HOME%.gradle-bootstrap
set DIST_DIR=%DIST_ROOT%\gradle-%GRADLE_VERSION%
set ZIP=%DIST_ROOT%\gradle-%GRADLE_VERSION%-bin.zip
if not exist "%DIST_DIR%\bin\gradle.bat" (
  if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'; Expand-Archive -Force '%ZIP%' '%DIST_ROOT%'"
  if errorlevel 1 exit /b 1
)
call "%DIST_DIR%\bin\gradle.bat" %*
endlocal
