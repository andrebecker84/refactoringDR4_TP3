@echo off
setlocal enabledelayedexpansion

title refactoringDR4_TP3

:: ---------- pre-requisitos ----------
where java >nul 2>&1 || (
    echo [ERRO] Java nao encontrado. Instale o JDK 21 e adicione ao PATH.
    pause & exit /b 1
)
where mvn >nul 2>&1 || (
    echo [ERRO] Maven nao encontrado. Instale o Maven 3.9+ e adicione ao PATH.
    pause & exit /b 1
)

:menu
cls
echo ============================================================
echo   refactoringDR4_TP3 -- E-Commerce Order Refactoring
echo ============================================================
echo.
echo   [1] Compilar
echo   [2] Executar testes
echo   [3] Build completo + cobertura JaCoCo
echo   [4] Abrir relatorio JaCoCo
echo   [5] Limpar build
echo   [0] Sair
echo.
set /p opcao="Escolha: "

if "%opcao%"=="1" goto compilar
if "%opcao%"=="2" goto testes
if "%opcao%"=="3" goto verify
if "%opcao%"=="4" goto relatorio
if "%opcao%"=="5" goto limpar
if "%opcao%"=="0" exit /b 0
goto menu

:compilar
mvn clean compile
pause & goto menu

:testes
mvn test
pause & goto menu

:verify
mvn clean verify
pause & goto menu

:relatorio
set REPORT=target\site\jacoco\index.html
if not exist "%REPORT%" (
    echo Relatorio nao encontrado. Execute a opcao [3] primeiro.
    pause & goto menu
)
start "" "%REPORT%"
goto menu

:limpar
mvn clean
echo Build removido.
pause & goto menu