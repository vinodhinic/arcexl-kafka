package com.arcexl.runner;

import com.arcexl.domain.StockPrice;
import com.arcexl.reader.StockPriceReader;
import com.arcexl.writer.StockPriceWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@ConditionalOnProperty(value = "scrapeStockPriceFromKafka", havingValue = "true")
@Service
public class KafkaStockPriceRunnable implements StockPriceRunnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStockPriceRunnable.class);

    @Autowired
    @Qualifier("kafkaStockPriceReader")
    private StockPriceReader stockPriceReader;

    private volatile boolean isTerminated = false;

    @Autowired
    private StockPriceWriter stockPriceWriter;

    @Override
    public void run() {
        while (!isTerminated) { // Only difference against FeedStockPriceRunnable - this has to keep reading from kafka until the app shuts down
            List<StockPrice> stockPrices = stockPriceReader.read();
            for (int i = 0; i < stockPrices.size(); i++) {
                stockPriceWriter.writeStockPrice(stockPrices.get(i));
                if (i % 2 == 0) {
                    try {
                        // Intentionally slowing down the rate of consumption.
                        LOGGER.info("Sync sleeping for every 2 price writes");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {
        this.isTerminated = true;
    }

}
