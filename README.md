# Flink/Kafka demo
## Installation
Require Zookeeper, Kafka & Flink
```bash
mkdir platform
cd platform
wget http://mirrors.standaloneinstaller.com/apache/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz
wget http://mirror.ibcp.fr/pub/apache/kafka/1.1.0/kafka_2.11-1.1.0.tgz
wget http://apache.mediamirrors.org/flink/flink-1.5.0/flink-1.5.0-bin-scala_2.11.tgz
tar -xzf zookeeper-3.4.12.tar.gz
tar -xzf kafka_2.11-1.1.0.tgz
tar -xzf flink-1.5.0-bin-scala_2.11.tgz
```
## Configuration
### Zookeeper
```bash
mv zookeeper-3.4.12/conf/zoo_sample.cfgas zookeeper-3.4.12/conf/zoo.cfg
mkdir zookeeper-3.4.12/data
```
in `zookeeper-3.4.12/conf/zoo.cfg` set `dataDir` to `/<zookeeper_home_directory>/data`
### Kafka
in `kafka_2.11-1.1.0/config/server.properties` set:
- `listeners` to `PLAINTEXT://:9092` (commented by default)
- `log.dirs` to `/<kafka_home_directory>/kafka-logs`
## Start
```bash
./zookeeper-3.4.12/bin/zkServer.sh start zoo.cfg
./kafka_2.11-1.1.0/bin/kafka-server-start.sh kafka_2.11-1.1.0/config/server.properties
./flink-1.5.0/bin/start-cluster.sh
```
## Stop
```bash
./flink-1.5.0/bin/stop-cluster.sh
./kafka_2.11-1.1.0/bin/kafka-server-stop.sh
./zookeeper-3.4.12/bin/zkServer.sh stop
```