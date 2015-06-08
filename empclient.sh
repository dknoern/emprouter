CLASSPATH=bin
for file in `ls lib/*.jar`;do
CLASSPATH=$file:$CLASSPATH
done
#echo CLASSPATH = $CLASSPATH
java -classpath $CLASSPATH com.seattleweb.emprouter.EmpClient $*

