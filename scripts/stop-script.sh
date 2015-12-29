ssh tomas@147.251.43.129 << KAFKA
echo 'stoping kafka'
masters-thesis/tools/kafka/bin/kafka-server-stop.sh
echo 'kafka stopped'
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
