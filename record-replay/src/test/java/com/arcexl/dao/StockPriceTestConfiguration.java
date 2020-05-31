package com.arcexl.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.KafkaContainer;

import javax.sql.DataSource;

@Configuration
public class StockPriceTestConfiguration {

    @Bean(name = "testPostgres", initMethod = "start", destroyMethod = "stop")
    public TestPostgres testPostgres() {
        return new TestPostgres();
    }

    @Primary
    @Bean("datasource")
    @DependsOn("testPostgres")
    public DataSource dataSource(TestPostgres container) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(container.getJdbcUrl() + "&currentSchema=arcexl");
        ds.setUsername(container.getUsername());
        ds.setPassword(container.getPassword());
        return ds;
    }

    @Bean(name = "testKafka", initMethod = "start", destroyMethod = "stop")
    public KafkaContainer kafkaContainer() {
        KafkaContainer kafkaContainer = new KafkaContainer();
        return kafkaContainer;
    }

    @Primary
    @Bean("kafkaBootstrapServer")
    @DependsOn("testKafka")
    public String port(KafkaContainer kafkaContainer) {
        return kafkaContainer.getBootstrapServers();
    }
}
