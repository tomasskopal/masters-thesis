#!/bin/bash
ssh tomas@147.251.43.129 << 'CONSUMER'
cd masters-thesis/app/main-app/target
~/Downloads/jdk1.8.0_65/bin/java -jar main-app.jar -ip 147.251.43.129 -zklist 147.251.43.129:2181,147.251.43.130:2181 -zkpath / -m consumer > /dev/null &
echo $! > ~/consumer.pid
CONSUMER
for server in 147.251.43.129 147.251.43.130
do
ssh tomas@${server} << 'PRODUCER'
cd masters-thesis/app/main-app/target
~/Downloads/jdk1.8.0_65/bin/java -jar main-app.jar -ip 147.251.43.130 -zklist 147.251.43.129:2181,147.251.43.130:2181 -zkpath /147.251.43.129 -m producer -p 147.251.43.129 > /dev/null &
sleep 1s
echo $! > ~/producer.pid
PRODUCER
done