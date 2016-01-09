#!/bin/bash
for server in 147.251.43.129 147.251.43.130 147.251.43.150 147.251.43.138
do
ssh tomas@${server} << ZK
echo 'starting zookeeper'
masters-thesis/tools/zookeeper-cli/bin/zkServer.sh start
echo 'zookeeper started'
ZK
done
sleep 5s
ssh tomas@147.251.43.129 << KAFKA
echo 'starting kafka'
masters-thesis/tools/kafka/bin/kafka-server-start.sh masters-thesis/tools/kafka/config/server.properties > /dev/null &
echo 'kafka started'
KAFKA
sleep 5s
