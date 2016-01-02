ssh tomas@147.251.43.129 << CONSUMER
cd masters-thesis/app/main-app/target
~/Downloads/jdk1.8.0_65/bin/java -jar main-app.jar -ip 147.251.43.129 -zklist 147.251.43.129:2181,147.251.43.130:2181 -zkpath / -m combined &
echo $! > ~/consumer.pid
CONSUMER
ssh tomas@147.251.43.130 << PRODUCER
cd masters-thesis/app/main-app/target
~/Downloads/jdk1.8.0_65/bin/java -jar main-app.jar -ip 147.251.43.130 -zklist 147.251.43.129:2181,147.251.43.130:2181 -zkpath /147.251.43.130 -m producer -p 147.251.43.129 &
echo $! > ~/producer.pid
PRODUCER