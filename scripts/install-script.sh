#!/bin/bash
for server in 147.251.43.129 147.251.43.130 147.251.43.150 147.251.43.138
do
ssh tomas@${server} << EOF
export JAVA_HOME=/home/tomas/Downloads/jdk1.8.0_65
export MAVEN_OPTS="-Xmx512m"
cd masters-thesis
git pull
cd app
mvn install -DskipTests
EOF
done