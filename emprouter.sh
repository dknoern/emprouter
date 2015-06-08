CLASSPATH=bin
for file in `ls lib/*.jar`;do
CLASSPATH=$file:$CLASSPATH
done
java -classpath $CLASSPATH com.seattleweb.emprouter.EmpRouter


