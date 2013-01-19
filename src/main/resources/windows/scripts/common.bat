@ECHO OFF
SET HERE=%~dp0
IF %HERE:~-1%==\ SET HERE=%HERE:~0,-1%

SET MAXPERMSIZE=$maxPermSizeMb$
SET MAXHEAPSIZE=$maxHeapSizeMb$

SET JRE_HOME=%HERE%\jre
SET CATALINA_HOME=%HERE%\tomcat
set CATALINA_BASE=%HERE%
SET REALCONFDIR=%CATALINA_BASE%\conf
SET CATALINA_OPTS=-Xmx%MAXHEAPSIZE%m -XX:MaxPermSize=%MAXPERMSIZE%m -Dfile.encoding=UTF-8
SET PATH=%PATH%;%CATALINA_HOME%\bin
SET SERVICE_NAME=$serviceName$
set EXECUTABLE=%CATALINA_HOME%\bin\tomcat7.exe
SET MECEAPLOGS=%HERE%\logs
set LOGGING_CONFIG=-Djava.util.logging.config.file=%REALCONFDIR%\logging.properties
mkdir %MECEAPLOGS%

set PR_DESCRIPTION=$serviceDescription$
set PR_INSTALL=%EXECUTABLE%
set PR_CLASSPATH=%CATALINA_HOME%\bin\bootstrap.jar;%CATALINA_BASE%\bin\tomcat-juli.jar;%CATALINA_HOME%\bin\tomcat-juli.jar
set PR_JVM=%JRE_HOME%\bin\server\jvm.dll
set PR_LOGPATH=%MECEAPLOGS%
set PR_STDOUTPUT=auto
set PR_STDERROR=auto


