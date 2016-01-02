ssh tomas@147.251.43.129 << CONSUMER
kill -9 `cat consumer.pid`
echo "consumer killed"
CONSUMER
ssh tomas@147.251.43.130 << PRODUCER
kill -9 `cat producer.pid`
echo "producer killed"
PRODUCER