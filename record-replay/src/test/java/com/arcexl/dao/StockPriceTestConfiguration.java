package com.arcexl.dao;

import org.springframework.context.annotation.Configuration;

@Configuration
public class StockPriceTestConfiguration {
    static {
        TestPostgres testPostgres = TestPostgres.getInstance();
    }
}
