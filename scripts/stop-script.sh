ssh tomas@147.251.43.129 << KAFKA
echo 'stoping kafka'
masters-thesis/tools/kafka/bin/kafka-server-stop.sh
echo 'kafka stopped'
echo 'clearing ZK tree'
~/Downloads/jdk1.8.0_65/bin/java -jar masters-thesis/app/cleaner/target/cleaner.jar
KAFKA
sleep 5s
for server in 147.251.43.129 147.251.43.130
do
ssh tomas@${server} << ZK
echo 'stoping zookeeper'
masters-thesis/tools/zookeeper-cli/bin/zkServer.sh stop
echo 'zookeeper stopped'
ZK
done