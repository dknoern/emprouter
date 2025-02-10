CLASSPATH=bin
for file in `ls lib/*.jar`;do
CLASSPATH=$file:$CLASSPATH
done
#echo CLASSPATH = $CLASSPATH

CLASSPATH=target/emprouter-1.0-SNAPSHOT-jar-with-dependencies.jar

java -classpath $CLASSPATH com.seattleweb.emprouter.EmpClient $*

