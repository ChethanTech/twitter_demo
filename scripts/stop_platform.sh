#!/usr/bin/env bash

source ../config/global.cfg
cd "$platformPath"

./flink-1.5.2/bin/stop-cluster.sh
./kafka_2.11-1.1.0/bin/kafka-server-stop.sh
./zookeeper-3.4.12/bin/zkServer.sh stop
kill -SIGTERM "$(< /tmp/cerebro.pid)"
kill -SIGTERM "$(< /tmp/kibana.pid)"
kill -SIGTERM "$(< /tmp/logstash.pid)"
kill -SIGTERM "$(< /tmp/elasticsearch.pid)"
kill -SIGTERM "$(< kafka-manager-1.3.3.17/RUNNING_PID)"