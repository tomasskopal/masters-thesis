#!/bin/bash
for server in 147.251.43.129 147.251.43.130
do
ssh tomas@${server} << EOF
export JAVA_HOME=/home/tomas/Downloads/jdk1.8.0_65
cd masters-thesis
git pull
cd app
mvn clean install -DskipTests
EOF
done