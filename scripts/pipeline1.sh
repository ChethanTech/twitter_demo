#!/usr/bin/env bash

source ../config/global.cfg
source ../config/pipeline1.cfg
dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd "$platformPath"

./flink-1.5.2/bin/flink run -d "$dir/../processing_examples/classify_tweets/target/classify_tweets-0.1.jar" \
    --consumer.bootstrap.servers "localhost:9092" \
    --producer.bootstrap.servers "localhost:9092" \
    --consumer.group.id "g1" \
    --consumer.topic.id "pipeline1_raw_tweets" \
    --producer.topic.id "pipeline1_rich_tweets" \
    --classification-file "$classificationFile"

./flink-1.5.2/bin/flink run -d "$dir/../twitter_stream_to_kafka/target/twitter_stream_to_kafka-0.1.jar" \
    --uri "/1.1/statuses/filter.json?track=$trackedKeywords" \
    --http-method "GET" \
    --twitter-source.consumerKey "$consumerKey" \
    --twitter-source.consumerSecret "$consumerSecret" \
    --twitter-source.token "$token" \
    --twitter-source.tokenSecret "$tokenSecret" \
    --bootstrap.servers "localhost:9092" \
    --topic.id "pipeline1_raw_tweets"

