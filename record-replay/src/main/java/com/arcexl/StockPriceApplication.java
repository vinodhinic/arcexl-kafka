package com.arcexl;

import com.arcexl.runner.StockPriceRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/*
Run with VM option to assume prod/uat profile :
-Dspring.profiles.active=<prod/uat>
*/
@SpringBootApplication
public class StockPriceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockPriceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(StockPriceApplication.class, args);
        StockPriceRunner stockPriceRunner = applicationContext.getBean(StockPriceRunner.class);
        stockPriceRunner.start();
        /*
         Stop button in your IDE does not send SIGTERM. https://youtrack.jetbrains.com/issue/CPP-3434
         Shutdown hooks work only for SIGTERM and not SIGKILL
         This code is to test if the shutdown hook is working fine.
         In ideal production scenario, container receives SIGTERM so rest assured the clean ups will execute before shutdown

        Thread.sleep(10000);
        StockPriceRunner stockPriceRunner = applicationContext.getBean(StockPriceRunner.class);
        stockPriceRunner.stop();
        */
    }
}
