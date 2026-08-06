@echo off
title [MineZone] Servidor Oficial - Paper 1.21.11
mode con: cols=90 lines=30
color 0F

:menu
cls
echo.
echo  ╔═══════════════════════════════════════════════════════════════╗
echo  ║                                                               ║
echo  ║  ███╗   ███╗██╗███╗   ██╗███████╗███████╗ ██████╗ ███╗   ██╗███████╗
echo  ║  ████╗ ████║██║████╗  ██║╚══███╔╝╚══███╔╝██╔═══██╗████╗  ██║╚══███╔╝
echo  ║  ██╔████╔██║██║██╔██╗ ██║  ███╔╝  ███╔╝ ██║   ██║██╔██╗ ██║  ███╔╝
echo  ║  ██║╚██╔╝██║██║██║╚██╗██║ ███╔╝  ███╔╝  ██║   ██║██║╚██╗██║ ███╔╝
echo  ║  ██║ ╚═╝ ██║██║██║ ╚████║███████╗███████╗╚██████╔╝██║ ╚████║███████╗
echo  ║  ╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚══════╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚══════╝
echo  ║                                                               ║
echo  ║              ──── Servidor Oficial ────                      ║
echo  ║              Versão: Paper 1.21.11                           ║
echo  ║              RAM Alocada: 4 GB                               ║
echo  ║              IP: katherine-shakira.tun.ply.gg:25565          ║
echo  ║                                                               ║
echo  ╚═══════════════════════════════════════════════════════════════╝
echo.
echo  [1] ▶  Iniciar Servidor
echo  [2] ⏹  Sair
echo.
set /p opcao="Escolha uma opção (1 ou 2): "

if "%opcao%"=="1" goto iniciar
if "%opcao%"=="2" goto sair
goto menu

:iniciar
cls
echo.
echo  ╔═══════════════════════════════════════════════════════════════╗
echo  ║            🚀 INICIANDO SERVIDOR OFICIAL...                  ║
echo  ║                                                               ║
echo  ║  ⏳ Aguarde enquanto o servidor carrega...                   ║
echo  ║  🌐 IP: katherine-shakira.tun.ply.gg:25565                  ║
echo  ║  📌 Para parar, digite "stop" no console.                   ║
echo  ╚═══════════════════════════════════════════════════════════════╝
echo.
timeout /t 2 /nobreak >nul
start /wait java -Xms4G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -jar server.jar nogui
cls
echo.
echo  ╔═══════════════════════════════════════════════════════════════╗
echo  ║                                                               ║
echo  ║  ⚠️  O servidor foi ENCERRADO.                               ║
echo  ║                                                               ║
echo  ║  [1] 🔄  Reiniciar Servidor                                  ║
echo  ║  [2] ⏹  Sair (fechar o script)                              ║
echo  ║                                                               ║
echo  ╚═══════════════════════════════════════════════════════════════╝
echo.
set /p opcao="Escolha uma opção (1 ou 2): "

if "%opcao%"=="1" goto iniciar
if "%opcao%"=="2" goto sair
goto menu

:sair
cls
echo.
echo  ╔═══════════════════════════════════════════════════════════════╗
echo  ║                                                               ║
echo  ║  👋 Saindo do script...                                      ║
echo  ║  Obrigado por usar o MineZone!                               ║
echo  ║  Até a próxima!                                              ║
echo  ║                                                               ║
echo  ╚═══════════════════════════════════════════════════════════════╝
echo.
timeout /t 2 /nobreak >nul
exit