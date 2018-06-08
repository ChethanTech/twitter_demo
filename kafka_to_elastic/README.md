# Kafka to Elasticsearch
```bash
./platform/flink-1.5.0/bin/flink run twitter_demo/kafka_to_elastic/target/kafka_to_elastic-0.1.jar \
    --bootstrap.servers "<server1[,server2,...]>" \
    --group.id "<id>" \
    --topic.id "<id>" \
    --nodes "<node1[,node2,...]>" \
    --cluster.name "<name>" \
    --index.name "<name>" \
    --type.name "<name>"
```
- `bootstrap.servers` A comma separated list of Kafka brokers
    - _Default: `localhost:9092`_
- `group.id` The consumer group this process is consuming on behalf of
    - **Required**
- `topic.id` The name of the Kafka topic to read from
    - **Required**
- `nodes` A comma separated list of Elasticsearch nodes to connect to
    - _Default: `localhost:9300`_
- `cluster.name` The Elasticsearch cluster to write to
    - _Default: `elasticsearch`_
- `index.name` The Elasticsearch index to write to
    - **Required**
- `type.name` An Elasticsearch type name
    - **Required**
    