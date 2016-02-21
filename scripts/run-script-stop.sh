for server in 147.251.43.130 147.251.43.150 147.251.43.138
do
ssh tomas@${server} << 'PRODUCER'
kill -9 `cat ~/app.pid`
echo "app killed"
PRODUCER
done
ssh tomas@147.251.43.129 << 'CONSUMER'
kill -9 `cat ~/app.pid`
echo "app killed"
CONSUMER