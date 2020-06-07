# Start zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka Server
bin/kafka-server-start.sh config/server.properties

# Create topic
bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic stockPriceTopic --create --partitions 3 --replication-factor 1

# Delete topic
bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic stockPriceTopic --delete

# List consumer groups
bin/kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list

# Describe Consumer groups
bin/kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --group stock-price-app --describe