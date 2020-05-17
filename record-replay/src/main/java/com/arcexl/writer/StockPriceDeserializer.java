package com.arcexl.writer;

import com.arcexl.domain.StockPrice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StockPriceDeserializer implements Deserializer<StockPrice> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockPriceDeserializer.class);

    @Override
    public StockPrice deserialize(String topic, byte[] data) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // important to handle LocalDate

        try {
            return objectMapper.readValue(data, StockPrice.class);
        } catch (IOException e) {
            LOGGER.error("Error deserializing stock price {}. Got exception : {}", data, e.getMessage());
        }
        return null;
    }
}
