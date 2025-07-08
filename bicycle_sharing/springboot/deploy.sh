#!/bin/bash

# 推荐在 pom.xml 中固定 finalName，这样这里就不用改了
JAR_NAME="springboot-0.0.1-SNAPSHOT.jar"
APP_PATH="/home/ubuntu/app"

PID=$(pgrep -f "java -jar $APP_PATH/$JAR_NAME")
if [ -n "$PID" ]; then
    echo "Killing old process: $PID"
    kill -9 $PID
fi

mkdir -p $APP_PATH
mv /home/ubuntu/$JAR_NAME $APP_PATH/$JAR_NAME

echo "Starting new process..."
nohup java -jar $APP_PATH/$JAR_NAME > $APP_PATH/app.log 2>&1 &

echo "Deployment finished."