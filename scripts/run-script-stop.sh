for server in 147.251.43.130 147.251.43.150 147.251.43.138 147.251.43.129
do
ssh tomas@${server} << 'APP'
kill -9 `cat ~/app.pid`
echo "app killed"
APP
done