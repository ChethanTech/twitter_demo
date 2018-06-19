# Raw statuses indexing in Elasticsearch
```bash
./platform/logstash-6.3.0/bin/logstash -f "twitter_demo/logstash_jobs/raw_statuses_indexing.conf"
```
**Note:** Before running this job for the first time, you need to create the following Elasticsearch index:
```bash
curl -XPUT 'http://localhost:9200/raw_statuses' -d '{
 "mappings": {
  "doc": {
   "dynamic": false,
   "_source": {
    "enabled": false
   },
   "properties": {
    "created_at": {
     "type": "date",
     "format": "EEE MMM dd HH:mm:ss +0000 yyyy",
     "store": true
    },
    "text": {
     "type": "text",
     "store": true
    },
    "quoted_text": {
     "type": "text",
     "store": true
    },
    "retweeted_text": {
     "type": "text",
     "store": true
    },
    "message": {
     "type": "text",
     "store": true
    }
   }
  }
 }
}'
```
