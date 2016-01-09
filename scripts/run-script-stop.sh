for server in 147.251.43.129 147.251.43.130 147.251.43.150 147.251.43.138
do
ssh tomas@${server} << 'PRODUCER'
kill -9 `cat ~/producer.pid`
echo "producer killed"
PRODUCER
done
ssh tomas@147.251.43.129 << 'CONSUMER'
kill -9 `cat ~/consumer.pid`
echo "consumer killed"
~/Downloads/jdk1.8.0_65/bin/java -jar masters-thesis/app/cleaner/target/cleaner.jar
CONSUMER