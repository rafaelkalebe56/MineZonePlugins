@echo off
title [MineZone] Servidor Oficial
mode con: cols=80 lines=18
color 0F

:menu
cls
echo.
echo  =================================================================
echo       MINEZONE - SERVIDOR OFICIAL
echo  =================================================================
echo.
echo  [1] Iniciar Servidor
echo  [2] Sair
echo.
set /p opcao="Escolha uma opcao (1 ou 2): "

if "%opcao%"=="1" goto iniciar
if "%opcao%"=="2" goto sair
goto menu

:iniciar
cls
echo.
echo  =================================================================
echo       INICIANDO SERVIDOR...
echo  =================================================================
echo.
echo  Aguarde...
echo  Digite "stop" no console para parar.
echo.
timeout /t 2 /nobreak >nul
start /wait java -Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar server.jar nogui
cls
echo.
echo  =================================================================
echo       SERVIDOR ENCERRADO
echo  =================================================================
echo.
echo  [1] Reiniciar Servidor
echo  [2] Sair
echo.
set /p opcao="Escolha uma opcao (1 ou 2): "

if "%opcao%"=="1" goto iniciar
if "%opcao%"=="2" goto sair
goto menu

:sair
cls
echo.
echo  Saindo... Obrigado por usar o MineZone!
timeout /t 2 /nobreak >nul
exit
