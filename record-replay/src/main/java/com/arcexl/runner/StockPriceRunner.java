package com.arcexl.runner;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class StockPriceRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockPriceRunner.class);

    private final ExecutorService executorService;
    private final StockPriceRunnable stockPriceRunnable;

    @Autowired
    public StockPriceRunner(StockPriceRunnable stockPriceRunnable) {
        LOGGER.info("StockPriceRunner is initialized with Runnable {} ", stockPriceRunnable);
        this.stockPriceRunnable = stockPriceRunnable;
        executorService =
                Executors.newSingleThreadExecutor(
                        new ThreadFactoryBuilder().setNameFormat("stock-price-runner-%d").build());
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        LOGGER.info("Shutdown Hook invoked..");
                                        stop();
                                    } catch (InterruptedException e) {
                                        LOGGER.error("Exception while stopping the Dedup Runner", e);
                                    }
                                }));
    }

    public void start() {
        executorService.submit(stockPriceRunnable);
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        LOGGER.info("StockPriceRunner stop() invoked");
        this.stockPriceRunnable.shutdown();
        this.executorService.shutdown();
        if (!this.executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            LOGGER.info("Grace period exceeded. StockPriceRunner shutting down executor service");
            this.executorService.shutdownNow();
        } else {
            LOGGER.info("StockPriceRunner gracefully stopped");
        }
    }

}
