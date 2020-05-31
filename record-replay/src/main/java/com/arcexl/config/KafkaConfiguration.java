package com.arcexl.config;

import com.arcexl.writer.StockPriceDeserializer;
import com.arcexl.writer.StockPriceSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {

    @Bean("kafkaBootstrapServer")
    public String bootstrapServer(@Value("${kafka.bootstrap.server}") String kafkaBootstrapServer) {
        return kafkaBootstrapServer;
    }

    @Bean("kafkaProducerProperties")
    public Properties kafkaProducerProperties() {
        Properties kafkaProducerProperties = new Properties();
        kafkaProducerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer(null));
        kafkaProducerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProducerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StockPriceSerializer.class.getName());
        kafkaProducerProperties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        kafkaProducerProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "60000");
        return kafkaProducerProperties;
    }

    @Bean
    public Properties kafkaConsumerProperties(@Value("${stockPrice.consumer.group}") String consumerGroupId) {
        Properties kafkaConsumerProperties = new Properties();
        kafkaConsumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer(null));
        kafkaConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        kafkaConsumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaConsumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StockPriceDeserializer.class.getName());
        kafkaConsumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConsumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        kafkaConsumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return kafkaConsumerProperties;
    }
}
