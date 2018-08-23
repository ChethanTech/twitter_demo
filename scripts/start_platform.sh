#!/usr/bin/env bash

source conf.cfg
cd "$platformPath"

./zookeeper-3.4.12/bin/zkServer.sh start zookeeper-3.4.12/conf/zoo.cfg &
sleep 3
./kafka_2.11-1.1.0/bin/kafka-server-start.sh kafka_2.11-1.1.0/config/server.properties &
./flink-1.5.2/bin/start-cluster.sh &
./elasticsearch-6.3.0/bin/elasticsearch -p /tmp/elasticsearch.pid &
./logstash-6.3.0/bin/logstash &
echo $! > /tmp/logstash.pid
./kibana-6.3.0-darwin-x86_64/bin/kibana &
echo $! > /tmp/kibana.pid
./cerebro-0.7.3/bin/cerebro &
echo $! > /tmp/cerebro.pid
./kafka-manager-1.3.3.17/bin/kafka-manager -Dhttp.port=9001
