@echo off
setlocal
cd /d "%~dp0"
echo [DisplaySystem] Compilando com Maven...
mvn clean package
if errorlevel 1 (
  echo.
  echo [ERRO] A compilacao falhou.
  exit /b 1
)
echo.
echo [OK] JAR gerado em target\DisplaySystem.jar
endlocal
