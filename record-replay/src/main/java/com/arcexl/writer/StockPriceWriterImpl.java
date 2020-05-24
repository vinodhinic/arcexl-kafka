package com.arcexl.writer;

import com.arcexl.dao.StockPriceDao;
import com.arcexl.domain.StockPrice;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Properties;

@Service
public class StockPriceWriterImpl implements StockPriceWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockPriceWriterImpl.class);

    private final Boolean writeStockPriceToKafka;
    private final KafkaProducer<String, StockPrice> kafkaProducer;

    @Autowired
    private StockPriceDao stockPriceDao;

    @Value("${kafka.topic.stock_price.name}")
    private String topicName;

    public StockPriceWriterImpl(@Value("${kafka.bootstrap.server}") String kafkaBootstrapServer,
                                @Value("${writeStockPriceToKafka}") Boolean writeStockPriceToKafka) {
        LOGGER.info("StockPriceWriter initialized with writeStockPriceToKafka : {}", writeStockPriceToKafka);
        Properties kafkaProducerProperties = new Properties();
        kafkaProducerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        kafkaProducerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProducerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StockPriceSerializer.class.getName());
        kafkaProducerProperties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        kafkaProducerProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "60000");
        this.kafkaProducer = new KafkaProducer<>(kafkaProducerProperties);
        this.writeStockPriceToKafka = writeStockPriceToKafka;
    }

    @Override
    public void writeStockPrice(StockPrice stockPrice) {
        try {
            stockPriceDao.insertStockPrice(stockPrice);

            if (writeStockPriceToKafka) {
                ProducerRecord<String, StockPrice> producerRecord = new ProducerRecord<>(topicName, stockPrice.getStockSymbol(), stockPrice);

                // Producing message to Kafka is asynchronous. I have added a callback to print the response
                kafkaProducer.send(producerRecord, (metadata, exception) -> {
                    if (exception == null) {
                        LOGGER.info("StockPrice {} was successfully sent. Received metadata : \n" +
                                        "Topic : {} \n " +
                                        "Partition : {} \n" +
                                        "Offset : {} \n" +
                                        "Timestamp : {}",
                                stockPrice,
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset(),
                                metadata.timestamp()
                        );
                    } else {
                        LOGGER.error("Unable to produce stockPrice {} into topic. Got : {}", stockPrice, exception.getMessage());
                    }
                });
            }
        } catch (Throwable e) {
            LOGGER.info("Stock Price {} didn't make it to DB or Topic due to exception {}", stockPrice, e.getMessage());
        }
    }

    @PreDestroy
    public void cleanUp() {
        this.kafkaProducer.flush();
        this.kafkaProducer.close();
    }
}
