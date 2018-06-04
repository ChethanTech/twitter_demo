# Flink/Kafka demo
## Platform
### Installation
```bash
git clone https://github.com/ndrpnt/flink-kafka-demo.git
mkdir platform
cd platform
```
#### Zookeeper
```bash
wget http://mirrors.standaloneinstaller.com/apache/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz
tar -xzf zookeeper-3.4.12.tar.gz
mv zookeeper-3.4.12/conf/zoo_sample.cfg zookeeper-3.4.12/conf/zoo.cfg
mkdir zookeeper-3.4.12/data
```
In `zookeeper-3.4.12/conf/zoo.cfg` set `dataDir` to `/<zookeeper_home_directory>/data`
#### Kafka
```bash
wget http://mirror.ibcp.fr/pub/apache/kafka/1.1.0/kafka_2.11-1.1.0.tgz
tar -xzf kafka_2.11-1.1.0.tgz
```
In `kafka_2.11-1.1.0/config/server.properties` set:
- `listeners` to `PLAINTEXT://:9092` (commented by default)
- `log.dirs` to `/<kafka_home_directory>/kafka-logs`
#### Flink
```bash
wget http://apache.mediamirrors.org/flink/flink-1.5.0/flink-1.5.0-bin-scala_2.11.tgz
tar -xzf flink-1.5.0-bin-scala_2.11.tgz
```
#### Elasticsearch
```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.9.tar.gz
tar -xzf elasticsearch-5.6.9.tar.gz
```
#### Kibana
##### Linux
```bash
wget https://artifacts.elastic.co/downloads/kibana/kibana-5.6.9-linux-x86_64.tar.gz
tar -xzf kibana-5.6.9-linux-x86_64.tar.gz
```
##### Mac
```bash
wget https://artifacts.elastic.co/downloads/kibana/kibana-5.6.9-darwin-x86_64.tar.gz
tar -xzf kibana-5.6.9-darwin-x86_64.tar.gz
```
### Start
```bash
./zookeeper-3.4.12/bin/zkServer.sh start zoo.cfg
./kafka_2.11-1.1.0/bin/kafka-server-start.sh kafka_2.11-1.1.0/config/server.properties
# Create a new topic named `streaming.twitter.statuses` (only once)
./kafka_2.11-1.1.0/bin/kafka-topics.sh --create \
    --zookeeper "localhost:2181" \
    --replication-factor 1 \
    --partitions 1 \
    --topic "<topic_name>"
./flink-1.5.0/bin/start-cluster.sh
./elasticsearch-5.6.9/bin/elasticsearch
./kibana-5.6.9-darwin-x86_64/bin/kibana
```
### Stop
```bash
./flink-1.5.0/bin/stop-cluster.sh
./kafka_2.11-1.1.0/bin/kafka-server-stop.sh
./zookeeper-3.4.12/bin/zkServer.sh stop
```
## Flink Jobs
```bash
cd ../flink-kafka-demo
mvn clean package
cd ..
```
### Twitter stream to kafka
```bash
./platform/flink-1.5.0/bin/flink run flink-kafka-demo/twitter_stream_to_kafka/target/twitter_stream_to_kafka-0.1.jar \
    --uri "<uri>" \
    --http-method "<method>" \
    --twitter-source.consumerKey "<key>" \
    --twitter-source.consumerSecret "<secret>" \
    --twitter-source.token "<token>" \
    --twitter-source.tokenSecret "<tokenSecret>" \
    --bootstrap.servers "<servers>" \
    --topic.id "<id>"
```
### Twitter word count
```bash
./platform/flink-1.5.0/bin/flink run flink-kafka-demo/twitter_word_count/target/twitter_word_count-0.1.jar \
    --bootstrap.servers "<connect>" \
    --zookeeper.connect "<servers>" \
    --topic.id "<id>"
```