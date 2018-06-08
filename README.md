# Twitter demo
## Platform installation
```bash
git clone https://github.com/ndrpnt/flink-kafka-demo.git
mkdir platform
cd platform
```
### Zookeeper
```bash
wget http://mirrors.standaloneinstaller.com/apache/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz
tar -xzf zookeeper-3.4.12.tar.gz
mv zookeeper-3.4.12/conf/zoo_sample.cfg zookeeper-3.4.12/conf/zoo.cfg
mkdir zookeeper-3.4.12/data
```
In `zookeeper-3.4.12/conf/zoo.cfg` set `dataDir` to `/<zookeeper_home_directory>/data`
### Kafka
```bash
wget http://mirror.ibcp.fr/pub/apache/kafka/1.1.0/kafka_2.11-1.1.0.tgz
tar -xzf kafka_2.11-1.1.0.tgz
```
In `kafka_2.11-1.1.0/config/server.properties` set:
- `listeners` to `PLAINTEXT://:9092` (commented by default)
- `log.dirs` to `/<kafka_home_directory>/kafka-logs`
### Flink
```bash
wget http://apache.mediamirrors.org/flink/flink-1.5.0/flink-1.5.0-bin-scala_2.11.tgz
tar -xzf flink-1.5.0-bin-scala_2.11.tgz
```
In `flink-1.5.0/conf/fliink-conf.yaml` set `taskmanager.numberOfTaskSlots` to 10
### Elasticsearch
```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.9.tar.gz
tar -xzf elasticsearch-5.6.9.tar.gz
```
### Kibana
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
## Start platform
```bash
./zookeeper-3.4.12/bin/zkServer.sh start zoo.cfg
./kafka_2.11-1.1.0/bin/kafka-server-start.sh kafka_2.11-1.1.0/config/server.properties
./flink-1.5.0/bin/start-cluster.sh
./elasticsearch-5.6.9/bin/elasticsearch
./kibana-5.6.9-darwin-x86_64/bin/kibana
```
## Stop platform
```bash
./flink-1.5.0/bin/stop-cluster.sh
./kafka_2.11-1.1.0/bin/kafka-server-stop.sh
./zookeeper-3.4.12/bin/zkServer.sh stop
```
## Build Flink jobs
```bash
cd twitter_demo
mvn clean install
```
