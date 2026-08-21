@echo off
setlocal
cd /d "%~dp0"

rem Chamado pelo /restart do Paper. O start.bat principal detecta este marcador
rem depois que a JVM encerra e inicia o servidor novamente na mesma janela.
> ".minezone-restart" echo restart
>> "logs_inicializacao.log" echo [%date% %time%] Marcador de reinicio criado pelo Paper

endlocal
exit /b 0
