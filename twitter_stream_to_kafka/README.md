# Twitter stream to kafka
Connect to any twitter streaming endpoint (`https://stream.twitter.com/...`) and push the received content to a specified Kafka topic.

This could be achieved with Logstash twitter input plugin too, but would limit configuration options greatly.
## Usage
```bash
./platform/flink-1.5.0/bin/flink run twitter_demo/twitter_stream_to_kafka/target/twitter_stream_to_kafka-0.1.jar \
    --uri "<uri>" \
    --http-method "<method>" \
    --twitter-source.consumerKey "<key>" \
    --twitter-source.consumerSecret "<secret>" \
    --twitter-source.token "<token>" \
    --twitter-source.tokenSecret "<tokenSecret>" \
    --bootstrap.servers "<server1[,server2,...]>" \
    --topic.id "<id>"
```
- `uri`: The full uri, including the starting "/", the api version, and any query params. Only twitter streaming endpoints (`https://stream.twitter.com/...`) are considered valid
    - _Default: `/1.1/statuses/sample.json`_
- `http-method`: The HTTP request method
    - _Default: `GET`_
- `twitter-source.consumerKey`, `twitter-source.consumerSecret`, `twitter-source.token`, `twitter-source.tokenSecret`: Twitter credentials, can be obtained [here](https://developer.twitter.com/en/docs/basics/authentication/guides/access-tokens.html)
    - **Required**
- `bootstrap.servers` A comma separated list of Kafka brokers
    - _Default: `localhost:9092`_
- `topic.id` The name of the Kafka topic to write to
    - **Required**