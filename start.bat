@echo off
title [MineZone] Servidor Oficial
mode con: cols=80 lines=18
color 0F

if not exist server.jar (
    cls
    echo.
    echo  =================================================================
    echo       ERRO: server.jar nao encontrado
    echo  =================================================================
    echo.
    echo  Verifique se este .bat esta na mesma pasta do server.jar.
    echo.
    pause
    exit
)

:iniciar
cls
echo.
echo  =================================================================
echo       MINEZONE - SERVIDOR OFICIAL
echo  =================================================================
echo.
echo  Iniciando servidor...
echo  Digite "stop" no console para parar.
echo.
echo  [%date% %time%] Iniciando servidor >> logs_inicializacao.log
timeout /t 2 /nobreak >nul

java -Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar server.jar nogui

if %errorlevel% equ 0 (
    echo  [%date% %time%] Servidor encerrado normalmente >> logs_inicializacao.log
    cls
    echo.
    echo  =================================================================
    echo       SERVIDOR ENCERRADO
    echo  =================================================================
    echo.
    timeout /t 3 /nobreak >nul
    exit
) else (
    echo  [%date% %time%] Servidor caiu (codigo %errorlevel%^) - reiniciando... >> logs_inicializacao.log
    cls
    echo.
    echo  =================================================================
    echo       SERVIDOR CAIU - REINICIANDO EM 5 SEGUNDOS...
    echo  =================================================================
    echo.
    timeout /t 5 /nobreak >nul
    goto iniciar
)
