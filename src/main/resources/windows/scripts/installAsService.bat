"%EXECUTABLE%" //IS//%SERVICE_NAME% --StartClass org.apache.catalina.startup.Bootstrap --StopClass org.apache.catalina.startup.Bootstrap --StartParams start --StopParams stop
"%EXECUTABLE%" //US//%SERVICE_NAME% --JvmOptions "-Dcatalina.base=%CATALINA_BASE%;-Dcatalina.home=%CATALINA_HOME%;-Djava.endorsed.dirs=%CATALINA_HOME%\endorsed;-Dmeceaplogs=%MECEAPLOGS%" --StartMode jvm --StopMode jvm
"%EXECUTABLE%" //US//%SERVICE_NAME% ++JvmOptions "-Djava.io.tmpdir=%CATALINA_BASE%\temp;-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager;%LOGGING_CONFIG%" --JvmMx %MAXHEAPSIZE%
"%EXECUTABLE%" //US//%SERVICE_NAME% ++JvmOptions="-XX:MaxPermSize=%MAXPERMSIZE%m"
"%EXECUTABLE%" //US//%SERVICE_NAME% ++JvmOptions="-Dfile.encoding=UTF-8"

echo "Service installed."

pause
