# Count Hashtags
Output an ordered list of hashtags associated with their respective number of occurrences in a given time-window
## Usage
```bash
./platform/flink-1.5.0/bin/flink run twitter_demo/count_hashtags/target/count_hashtags-0.1.jar \
    --consumer.bootstrap.servers "<server1[,server2,...]>" \
    --consumer.group.id "<id>" \
    --consumer.topic.id "<id>" \
    --producer.bootstrap.servers "<server1[,server2,...]>" \
    --producer.topic.id "<id>" \
    --black-list "<word1[,word2,...]>" \
    --white-list "<word1[,word2,...]>" \
    --window-size "<seconds>" \
    --window-slide "<seconds>" \
    --display-only "<count>" \
    --min-occurrences "<count>"
```
## Options
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
- `black-list` A comma separated list of hashtags to ignore (case insensitive)
    - _Default: `""`_
- `white-list` A comma separated list of hashtags to count (case insensitive, has no effect if empty)
    - _Takes priority over `black-list` if both are non-empty_
    - _Default: `""`_
- `window-size` The size of the window in seconds
    - _Default: `60`_
- `window-slide` The frequency of output in seconds
    - _Default: `5`_
- `display-only` The maximum number of hashtags in a single output
    - _Default: `10`_
- `min-occurrences` The number of occurrence(s) needed in the current time-window for a hashtag to be part of the output
    - _Default: `1`_
## Example
Every 5 seconds, display the 5 most used Hashtags withing the last 5 minutes, excluding "yes", "no", "Yes", "NO", …
```bash
./platform/flink-1.5.0/bin/flink run twitter_demo/count_hashtags/target/count_hashtags-0.1.jar \
    --consumer.group.id "group1" \
    --consumer.topic.id "raw_statuses" \
    --producer.topic.id "hashtags_count" \
    --black-list "yes, no" \
    --window-size "300" \
    --display-only "5"
```
## Example output
```json
[{"maybe":10},{"hello":5},{"goodbye":3}]
```
## Notes & limitations
- All outputs are lowercase. As Hashtags are case-insensitive, they need to be grouped together so camelcase cannot be preserved
- In case of equal Hashtags count, the order is undefined as well which ones are retained when `display-only` value is reached
- When the window is empty, no output is generated
