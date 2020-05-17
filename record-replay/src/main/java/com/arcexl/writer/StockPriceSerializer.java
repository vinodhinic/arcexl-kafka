package com.arcexl.writer;

import com.arcexl.domain.StockPrice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockPriceSerializer implements Serializer<StockPrice> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockPriceSerializer.class);

    @Override
    public byte[] serialize(String topic, StockPrice data) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // important to handle LocalDate
        try {
            return objectMapper.writeValueAsString(data).getBytes();
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing Stock Price {}. Got exception : {}", data, e.getMessage());
        }
        return new byte[0];
    }
}
