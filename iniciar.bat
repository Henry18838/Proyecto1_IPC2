@echo off
echo Iniciando Horizontes sin Limites...

echo Iniciando Backend (Tomcat)...
set CATALINA_HOME=C:\Users\Pablo\apache-tomcat-9\apache-tomcat-9.0.117-windows-x64\apache-tomcat-9.0.117
set JAVA_HOME=C:\Program Files\Java\jdk-21
call "%CATALINA_HOME%\bin\startup.bat"

echo Esperando que Tomcat inicie...
timeout /t 5 /nobreak

echo Iniciando Frontend (Angular)...
start cmd /k "cd C:\Users\Pablo\OneDrive\Escritorio\Proyecto1_IPC2\frontend && ng serve"

echo Abriendo navegador...
timeout /t 8 /nobreak
start http://localhost:4200

echo Todo listo!