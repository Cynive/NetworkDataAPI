@echo off
echo ================================================
echo Building NetworkDataAPI
echo ================================================
echo.

cd /d "%~dp0"

echo Cleaning previous builds...
call mvn clean

echo.
echo Building all modules...
call mvn package -DskipTests

echo.
echo ================================================
echo Build Complete!
echo ================================================
echo.
echo Output JARs:
echo - networkdataapi-paper\target\NetworkDataAPI-Paper-1.0-SNAPSHOT.jar
echo - networkdataapi-bungee\target\NetworkDataAPI-Bungee-1.0-SNAPSHOT.jar
echo.

pause

