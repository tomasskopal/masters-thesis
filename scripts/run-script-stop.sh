for server in 147.251.43.129 147.251.43.130
do
ssh tomas@${server} << 'PRODUCER'
kill -9 `cat ~/producer.pid`
echo "producer killed"
PRODUCER
done
ssh tomas@147.251.43.129 << 'CONSUMER'
kill -9 `cat ~/consumer.pid`
echo "consumer killed"
CONSUMER