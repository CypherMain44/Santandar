@echo off
REM Uso: ejecutar_jmeter.bat [host] [puerto]   (requiere jmeter en el PATH)
set HOST=%1
if "%HOST%"=="" set HOST=localhost
set PORT=%2
if "%PORT%"=="" set PORT=8080
set OUT=resultados_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%
set OUT=%OUT: =0%
mkdir %OUT%
jmeter -n -t santandar_plan.jmx -Jhost=%HOST% -Jport=%PORT% -Jusers=100 -Jramp=30 -Jloops=10 -l %OUT%\resultados.jtl -e -o %OUT%\dashboard
echo Dashboard HTML: %OUT%\dashboard\index.html
