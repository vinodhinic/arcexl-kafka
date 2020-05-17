package com.arcexl.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.arcexl.dao.mybatis.mapper")
public class PersistenceConfiguration {
}
