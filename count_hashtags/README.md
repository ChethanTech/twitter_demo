# Count Hashtags
```bash
./platform/flink-1.5.0/bin/flink run twitter_demo/count_hashtags/target/count_hashtags-0.1.jar \
    --consumer.bootstrap.servers "<server1[,server2,...]>" \
    --consumer.group.id "<id>" \
    --consumer.topic.id "<id>" \
    --producer.bootstrap.servers "<server1[,server2,...]>" \
    --producer.topic.id "<id>"
```
- `consumer.bootstrap.servers` A comma separated list of Kafka brokers
    - _Default: `localhost:9092`_
- `consumer.group.id` The consumer group this process is consuming on behalf of
    - **Required**
- `consumer.topic.id` The name of the Kafka topic to read from
    - **Required**
- `producer.bootstrap.servers` A comma separated list of Kafka brokers
    - _Default: `localhost:9092`_
- `producer.topic.id` The name of the Kafka topic to write to
    - **Required**
    