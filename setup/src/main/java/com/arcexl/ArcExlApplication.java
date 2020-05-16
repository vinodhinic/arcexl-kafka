package com.arcexl;

import com.arcexl.dao.StockPriceDao;
import com.arcexl.domain.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;

/*
Run with VM option to assume prod/uat profile :
-Dspring.profiles.active=<prod/uat>
*/
@SpringBootApplication
public class ArcExlApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArcExlApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ArcExlApplication.class, args);
        StockPriceDao stockPriceDao = applicationContext.getBean("stockPriceDao", StockPriceDao.class);
        StockPrice stockPrice = new StockPrice("APPLE", LocalDate.now(), 89.00);
        stockPriceDao.insertStockPrice(stockPrice);
        LOGGER.info("Stock prices retrieved from DB {}", stockPriceDao.selectAllStockPrice());
        stockPriceDao.deleteStockPrice(stockPrice.getStockSymbol(), stockPrice.getDate());
    }
}
