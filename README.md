# Twitter demo
## Platform installation
```bash
git clone https://github.com/ndrpnt/flink-kafka-demo.git
mkdir platform
cd platform
```
### [Apache ZooKeeper](https://zookeeper.apache.org/)
ZooKeeper is a centralized service for maintaining configuration information, naming, providing distributed synchronization, and providing group services
```bash
wget http://mirrors.standaloneinstaller.com/apache/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz
tar -xzf zookeeper-3.4.12.tar.gz
mv zookeeper-3.4.12/conf/zoo_sample.cfg zookeeper-3.4.12/conf/zoo.cfg
mkdir zookeeper-3.4.12/data
```
In `zookeeper-3.4.12/conf/zoo.cfg`, set `dataDir` to `/<zookeeper_home_directory>/data`
### [Apache Kafka](https://kafka.apache.org/)
A distributed streaming platform
```bash
wget http://mirror.ibcp.fr/pub/apache/kafka/1.1.0/kafka_2.11-1.1.0.tgz
tar -xzf kafka_2.11-1.1.0.tgz
```
In `kafka_2.11-1.1.0/config/server.properties` set:
- `listeners` to `PLAINTEXT://:9092` (commented by default)
- `log.dirs` to `/<kafka_home_directory>/kafka-logs`
### [Apache Flink](https://flink.apache.org/)
A stream processing framework
```bash
wget http://apache.mediamirrors.org/flink/flink-1.5.0/flink-1.5.0-bin-scala_2.11.tgz
tar -xzf flink-1.5.0-bin-scala_2.11.tgz
```
In `flink-1.5.0/conf/fliink-conf.yaml` set `taskmanager.numberOfTaskSlots` to 10
### [Elasticsearch](https://www.elastic.co/fr/products/elasticsearch)
Elasticsearch is a distributed, RESTful search and analytics engine
```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.9.tar.gz
tar -xzf elasticsearch-5.6.9.tar.gz
```
### [Kibana](https://www.elastic.co/products/kibana)
Kibana lets you visualize your Elasticsearch data
#### Linux
```bash
wget https://artifacts.elastic.co/downloads/kibana/kibana-5.6.9-linux-x86_64.tar.gz
tar -xzf kibana-5.6.9-linux-x86_64.tar.gz
```
#### Mac
```bash
wget https://artifacts.elastic.co/downloads/kibana/kibana-5.6.9-darwin-x86_64.tar.gz
tar -xzf kibana-5.6.9-darwin-x86_64.tar.gz
```
### [Cerebro](https://github.com/lmenezes/cerebro)
An elasticsearch web admin
```bash
wget https://github.com/lmenezes/cerebro/releases/download/v0.7.3/cerebro-0.7.3.tgz
tar -xzf cerebro-0.7.3.tgz
```
### [Kafka Manager](https://github.com/yahoo/kafka-manager)
A tool for managing Apache Kafka
```bash
wget https://github.com/yahoo/kafka-manager/archive/1.3.3.17.tar.gz
tar -xzf 1.3.3.17.tar.gz
./1.3.3.17/sbt clean dist
unzip -a 1.3.3.17/target/universal/kafka-manager-1.3.3.17.zip
```
In `kafka-manager-1.3.3.17/conf/application.conf`, set `kafka-manager.zkhosts` to `"localhost:2181"`
## Start platform script
```bash
#!/bin/bash
       
./zookeeper-3.4.12/bin/zkServer.sh start zookeeper-3.4.12/conf/zoo.cfg &
./kafka_2.11-1.1.0/bin/kafka-server-start.sh kafka_2.11-1.1.0/config/server.properties &
./flink-1.5.0/bin/start-cluster.sh &
./elasticsearch-5.6.9/bin/elasticsearch -p /tmp/elasticsearch.pid &
./kibana-5.6.9-darwin-x86_64/bin/kibana &
echo $! > /tmp/kibana.pid
./cerebro-0.7.3/bin/cerebro &
echo $! > /tmp/cerebro.pid
./kafka-manager-1.3.3.17/bin/kafka-manager -Dhttp.port=9001
```
You then have access to:
- Flink at `http://localhost:8081`
- Kibana at `http://localhost:5601`
- Cerebro at `http://localhost:9000`
    - Connect to `http://localhost:9200`
- Kafka Manager at `http://localhost:9001`
    - Select add cluster and set:
        - A Cluster Name
        - Cluster Zookeeper Hosts to `localhost:2181`
        - Kafka Version to `1.0.0`
## Stop platform script
```bash
#!/bin/bash

./flink-1.5.0/bin/stop-cluster.sh
./kafka_2.11-1.1.0/bin/kafka-server-stop.sh
./zookeeper-3.4.12/bin/zkServer.sh stop
kill -SIGTERM "$(< /tmp/cerebro.pid)"
kill -SIGTERM "$(< /tmp/kibana.pid)"
kill -SIGTERM "$(< /tmp/elasticsearch.pid)"
kill -SIGTERM "$(< kafka-manager-1.3.3.17/RUNNING_PID)"
```
## Build Flink jobs
```bash
cd twitter_demo
mvn clean install
```
