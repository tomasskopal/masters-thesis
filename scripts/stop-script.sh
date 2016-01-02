ssh tomas@147.251.43.129 << KAFKA
echo 'stoping kafka'
masters-thesis/tools/kafka/bin/kafka-server-stop.sh
echo 'kafka stopped'
echo 'clearing tree'
export JAVA_HOME=/home/tomas/Downloads/jdk1.8.0_65
java -jar masters-thesis/app/cleaner/target/cleaner.jar
KAFKA
sleep 5s
ssh tomas@147.251.43.129 << ZK1
echo 'stoping zookeeper'
masters-thesis/tools/zookeeper-cli/bin/zkServer.sh stop
echo 'zookeeper stopped'
ZK1
ssh tomas@147.251.43.130 << ZK2
echo 'stoping zookeeper'
masters-thesis/tools/zookeeper-cli/bin/zkServer.sh stop
echo 'zookeeper stopped'
ZK2
