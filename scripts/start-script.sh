ssh tomas@147.251.43.129 << ZK1
echo 'starting zookeeper'
masters-thesis/tools/zookeeper-cli/bin/zkServer.sh start
echo 'zookeeper started'
ZK1
ssh tomas@147.251.43.130 << ZK2
echo 'starting zookeeper'
masters-thesis/tools/zookeeper-cli/bin/zkServer.sh start
echo 'zookeeper started'
ZK2
sleep 5s
ssh tomas@147.251.43.129 << KAFKA
echo 'starting kafka'
masters-thesis/tools/kafka/bin/kafka-server-start.sh masters-thesis/tools/kafka/config/server.properties > /dev/null &
echo 'kafka started'
KAFKA
sleep 5s
