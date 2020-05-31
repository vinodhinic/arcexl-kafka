package com.arcexl.reader;

import com.arcexl.domain.StockPrice;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service("kafkaStockPriceReader")
public class KafkaStockPriceReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStockPriceReader.class);

    private final KafkaConsumer<String, StockPrice> kafkaConsumer;

    public KafkaStockPriceReader(@Value("${kafka.topic.stock_price.name}") String topicName,
                                 @Autowired @Qualifier("kafkaConsumerProperties") Properties kafkaConsumerProperties) {
        kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
        LOGGER.info("Subscribing to Topic {} as Consumer group {}", topicName, kafkaConsumerProperties.get(ConsumerConfig.GROUP_ID_CONFIG));
        kafkaConsumer.subscribe(List.of(topicName));
    }

    public synchronized List<StockPrice> read() {
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

    public synchronized void commit() {
        this.kafkaConsumer.commitAsync((offsets, exception) -> {
            if (exception != null) {
                LOGGER.error("Unable to commit {} due to {}", offsets, exception.getMessage());
            } else {
                LOGGER.info("Committed offsets for topics : {} ", offsets);
            }
        });
    }

    @PreDestroy
    public void cleanUp() {
        this.kafkaConsumer.close();
    }
}
