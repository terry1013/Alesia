
set CLASSPATH=..
set CLASSPATH=%CLASSPATH%;lib\activejdbc-2.3.jar
set CLASSPATH=%CLASSPATH%;lib\jackson-annotations-2.9.0.jar
set CLASSPATH=%CLASSPATH%;lib\jackson-core-2.9.9.jar
set CLASSPATH=%CLASSPATH%;lib\jackson-databind-2.9.9.1.jar
set CLASSPATH=%CLASSPATH%;lib\javalite-common-2.3.jar
set CLASSPATH=%CLASSPATH%;lib\hsqldb.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.7.25.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-simple-1.7.25.jar

set CLASSPATH=%CLASSPATH%;build_time_libs\activejdbc-instrumentation-2.3.jar
set CLASSPATH=%CLASSPATH%;build_time_libs\javassist-3.18.2-GA.jar

java -classpath %CLASSPATH% -DoutputDirectory=.. org.javalite.instrumentation.Main
