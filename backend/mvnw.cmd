@REM Maven Wrapper 启动脚本
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set MAVEN_WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

@REM 从 repo1 下载 maven-wrapper.jar 如果不存在
if not exist %MAVEN_WRAPPER_JAR% (
    echo Downloading Maven Wrapper...
    set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
    curl -sL -o "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar" %WRAPPER_URL%
)

@REM 查找 Java
set JAVA_EXE=java.exe
where %JAVA_EXE% >nul 2>&1
if %ERRORLEVEL% neq 0 (
    set JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.11+10
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

@REM 执行 Maven Wrapper
"%JAVA_EXE%" ^
  -jar %MAVEN_WRAPPER_JAR% ^
  %*

endlocal
