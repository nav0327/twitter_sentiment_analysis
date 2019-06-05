# Twitter_sentiment_analysis
This project is a scala code that streams real-time tweets from twitter based on a search topic. The tweets are analysed for their sentiment using Cornell NLP library and are streamed into Kafka server and they are visualised using Elasticsearch, Kibana and Logstash.
## Run instructions
<ul>
cd {path}\confluent-5.2.1-2.12\confluent-5.2.1\bin\windows
  <li> zookeeper-server-start.bat ../../etc/kafka/zookeeper.properties &</li>
  <li> kafka-server-start.bat ../../etc/kafka/server.properties &</li>
  <li> kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic {topic_name} </li>
  <li> kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic {topic_name} --from-beginning </li>
  <li> spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class KafkaExample {jar path} {topic_name} {search string} </li>
cd <path>\elasticsearch-5.6.8\elasticsearch-5.6.8\bin
  <li> elasticsearch</li>
cd <path>\kibana-5.6.8-windows-x86\kibana-5.6.8-windows-x86\bin
  <li> kibana</li>
cd <path>\logstash-5.6.8\logstash-5.6.8
  <li> bin\logstash -f logstash-simple.conf</li>

## A sample of the visualization
![Image of Yaktocat](https://github.com/nav0327/twitter_sentiment_analysis/kafka_chart.PNG)
