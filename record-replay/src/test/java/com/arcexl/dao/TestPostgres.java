package com.arcexl.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgres extends PostgreSQLContainer<TestPostgres> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPostgres.class);

    private static final String IMAGE_VERSION = "postgres:11.1";

    public TestPostgres() {
        super(IMAGE_VERSION);
    }

    @Override
    public void start() {
        super.start();
        LOGGER.info("Test postgres started...");
    }

    @Override
    public void stop() {
        super.stop();
        LOGGER.info("Test postgres stopped...");
    }
}
