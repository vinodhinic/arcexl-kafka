package com.arcexl.reader;

import com.arcexl.domain.StockPrice;
import com.arcexl.writer.StockPriceDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service("kafkaStockPriceReader")
public class KafkaStockPriceReaderImpl implements StockPriceReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStockPriceReaderImpl.class);

    private final KafkaConsumer<String, StockPrice> kafkaConsumer;

    public KafkaStockPriceReaderImpl(@Value("${kafka.bootstrap.server}") String kafkaBootstrapServer,
                                     @Value("${stockPrice.consumer.group}") String consumerGroupId,
                                     @Value("${kafka.topic.stock_price.name}") String topicName) {
        Properties kafkaConsumerProperties = new Properties();
        kafkaConsumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        kafkaConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        kafkaConsumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaConsumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StockPriceDeserializer.class.getName());
        kafkaConsumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConsumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
        LOGGER.info("Subscribing to Topic {} as Consumer group {}", topicName, consumerGroupId);
        kafkaConsumer.subscribe(List.of(topicName));
    }

    @Override
    public List<StockPrice> read() {
        List<StockPrice> stockPrices = new ArrayList<>();

        ConsumerRecords<String, StockPrice> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(2));
        for (ConsumerRecord<String, StockPrice> consumerRecord : consumerRecords) {
            LOGGER.info("Read ConsumerRecord {} , partition {} and offset {} ", consumerRecord.value(), consumerRecord.partition()
                    , consumerRecord.offset());
            stockPrices.add(consumerRecord.value());
        }

        LOGGER.info("KafkaStockPriceReader read {} prices", stockPrices.size());
        return stockPrices;
    }

    @PreDestroy
    public void cleanUp() {
        this.kafkaConsumer.commitSync();// Commit the offset before shutdown
        this.kafkaConsumer.close();
    }
}
