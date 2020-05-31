package com.arcexl.kafka;

import com.arcexl.BaseArcExlTest;
import com.arcexl.dao.StockPriceDao;
import com.arcexl.domain.StockPrice;
import com.arcexl.reader.KafkaStockPriceReader;
import com.arcexl.writer.StockPriceWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class StockPriceKafkaTest extends BaseArcExlTest {

    @Autowired
    private StockPriceWriter stockPriceWriter;

    @Autowired
    private KafkaStockPriceReader kafkaStockPriceReader;

    @Autowired
    private StockPriceDao stockPriceDao;

    @Test
    public void testKafkaProducerAndConsumer() throws InterruptedException {
        StockPrice testStockPrice = new StockPrice("TEST-TESLA", LocalDate.now(), 1020.00);
        StockPrice testStockPrice2 = new StockPrice("TEST-TESLA", LocalDate.now(), 2020.00);

        stockPriceWriter.writeStockPrice(testStockPrice);
        stockPriceWriter.writeStockPrice(testStockPrice2);

        StockPrice stockPriceFromDB = stockPriceDao.selectStockPrice(testStockPrice.getStockSymbol(), testStockPrice.getDate());
        assertNotNull(stockPriceFromDB);
        assertEquals(testStockPrice2, stockPriceFromDB);

        CountDownLatch latch = new CountDownLatch(2);
        List<StockPrice> stockPricesFromKafka = new ArrayList<>();
        Executors.newSingleThreadExecutor().submit(
                () -> {
                    while (latch.getCount() > 0) {
                        List<StockPrice> stockPrices = kafkaStockPriceReader.read();
                        if (stockPrices != null) {
                            stockPrices.forEach(e -> {
                                latch.countDown();
                                stockPricesFromKafka.add(e);
                            });
                        }
                    }
                }
        );

        latch.await(60, TimeUnit.SECONDS);
        assertEquals(stockPricesFromKafka.size(), 2);
        assertTrue(stockPricesFromKafka.contains(testStockPrice));
        assertTrue(stockPricesFromKafka.contains(testStockPrice2));
    }
}
