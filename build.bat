@echo off
rem DepthCrawler + HACKnSLASH Weaponly ビルドスクリプト
setlocal

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
set MAVEN_HOME=C:\Users\MITSUK~1\AppData\Local\Temp\opencode\maven\apache-maven-3.9.6

call "%MAVEN_HOME%\bin\mvn.cmd" -f "%~dp0depth-crawler\pom.xml" clean package
if errorlevel 1 exit /b 1

call "%MAVEN_HOME%\bin\mvn.cmd" -f "%~dp0hacknslash-weaponly\pom.xml" clean package
if errorlevel 1 exit /b 1

echo.
echo ============================================
echo  ビルド完了
echo  depth-crawler\target\DepthCrawler-1.1.0.jar
echo  hacknslash-weaponly\target\HACKnSLASH-Weaponly-0.1.0.jar
echo ============================================
endlocal
