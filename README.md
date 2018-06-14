# Twitter demo
A simple demo using Flink, Kafka & Elasticsearch using twitter free streaming data
## Platform installation
Follow these steps to run the project locally
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
To be able to run multiple jobs on a local (one taskmanager) cluster, set `taskmanager.numberOfTaskSlots: 10` in `flink-1.5.0/conf/fliink-conf.yaml`.
#### Modify some Flink internals to write our logs in a Kafka topic
We need to modify some Flink internals to write our logs in a Kafka topic:
- Change `flink-1.5.0/conf/log4j.properties` to:
```properties
log4j.rootLogger=INFO, console

log4j.logger.akka=WARN
log4j.logger.org.apache.kafka=WARN
log4j.logger.org.apache.hadoop=WARN
log4j.logger.org.apache.zookeeper=WARN
log4j.logger.com.weefin=TRACE, kafka
# suppress the warning that hadoop native libraries are not loaded (irrelevant for the client)
log4j.logger.org.apache.hadoop.util.NativeCodeLoader=OFF
# suppress the irrelevant (wrong) warnings from the netty channel handler
log4j.logger.org.apache.flink.shaded.akka.org.jboss.netty.channel.DefaultChannelPipeline=ERROR

log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.Target=System.out
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n

log4j.appender.kafka=org.apache.kafka.log4jappender.KafkaLog4jAppender
log4j.appender.kafka.brokerList=localhost:9092
log4j.appender.kafka.topic=logs.flink
log4j.appender.kafka.layout=org.apache.log4j.PatternLayout
log4j.appender.kafka.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %c{1}:%L - %m%n
```
- Change `flink-1.5.0/conf/log4j-cli.properties` to:
```properties
log4j.rootLogger=INFO, console
log4j.logger.com.weefin=TRACE, kafka
# suppress the warning that hadoop native libraries are not loaded (irrelevant for the client)
log4j.logger.org.apache.hadoop.util.NativeCodeLoader=OFF
# suppress the irrelevant (wrong) warnings from the netty channel handler
log4j.logger.org.apache.flink.shaded.akka.org.jboss.netty.channel.DefaultChannelPipeline=ERROR

log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.Target=System.out
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n

log4j.appender.kafka=org.apache.kafka.log4jappender.KafkaLog4jAppender
log4j.appender.kafka.brokerList=localhost:9092
log4j.appender.kafka.topic=logs.flink
log4j.appender.kafka.layout=org.apache.log4j.PatternLayout
log4j.appender.kafka.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n
```
- Add the needed dependencies in `flink-1.5.0/lib`:
```bash
cd flink-1.5.0/lib
wget http://central.maven.org/maven2/org/apache/kafka/kafka-log4j-appender/1.1.0/kafka-log4j-appender-1.1.0.jar
wget http://central.maven.org/maven2/org/apache/kafka/kafka-clients/1.1.0/kafka-clients-1.1.0.jar
cd ../..
```
### [Elasticsearch](https://www.elastic.co/products/elasticsearch)
Elasticsearch is a distributed, RESTful search and analytics engine
```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.0.tar.gz
tar -xzf elasticsearch-6.3.0.tar.gz
```
### [Logstash](https://www.elastic.co/products/logstash)
Logstash is a data processing pipeline that ingests data from a multitude of sources simultaneously, transforms it, and then sends it to a "stash".
```bash
wget https://artifacts.elastic.co/downloads/logstash/logstash-6.3.0.tar.gz
tar -xzf logstash-6.3.0.tar.gz
```
### [Kibana](https://www.elastic.co/products/kibana)
Kibana lets you visualize your Elasticsearch data
#### Linux
```bash
wget https://artifacts.elastic.co/downloads/kibana/kibana-6.3.0-linux-x86_64.tar.gz
tar -xzf kibana-6.3.0-linux-x86_64.tar.gz
```
#### Mac
```bash
wget https://artifacts.elastic.co/downloads/kibana/kibana-6.3.0-darwin-x86_64.tar.gz
tar -xzf kibana-6.3.0-darwin-x86_64.tar.gz
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
cd kafka-manager-1.3.3.17
./sbt clean dist
cd ..
unzip -a kafka-manager-1.3.3.17/target/universal/kafka-manager-1.3.3.17.zip
```
In `kafka-manager-1.3.3.17/conf/application.conf`, set `kafka-manager.zkhosts` to `"localhost:2181"`
### Cleanup
You can safely remove `1.3.3.17`, `*.tgz` & `*.tar.gz` directories
## Start platform script
```bash
#!/usr/bin/env bash
       
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
- Flink dashboard at `http://localhost:8081`
- Kibana dashboard at `http://localhost:5601`
- Cerebro dashboard at `http://localhost:9000`
    - Connect to `http://localhost:9200`
- Kafka Manager dashboard at `http://localhost:9001`
    - Select add cluster and set:
        - A Cluster Name
        - Cluster Zookeeper Hosts to `localhost:2181`
        - Kafka Version to `1.0.0`
## Stop platform script
```bash
#!/usr/bin/env bash

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
mvn package
```
