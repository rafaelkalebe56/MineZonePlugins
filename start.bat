@echo off
title [MineZone] Servidor Oficial
mode con: cols=80 lines=18
color 0F

cls
echo.
echo  =================================================================
echo       MINEZONE - SERVIDOR OFICIAL
echo  =================================================================
echo.
echo  Iniciando servidor automaticamente...
echo  Digite "stop" no console para parar.
echo.
timeout /t 2 /nobreak >nul

:iniciar
java -Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar server.jar nogui

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
goto iniciar

:sair
cls
echo.
echo  Saindo... Obrigado por usar o MineZone!
timeout /t 2 /nobreak >nul
exit
