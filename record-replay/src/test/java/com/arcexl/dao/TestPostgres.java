package com.arcexl.dao;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgres extends PostgreSQLContainer<TestPostgres> {
    private static final String IMAGE_VERSION = "postgres:11.1";
    private static TestPostgres container;

    private TestPostgres() {
        super(IMAGE_VERSION);
    }

    public static TestPostgres getInstance() {
        if (container == null) {
            container = new TestPostgres();
        }
        container.start();
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
