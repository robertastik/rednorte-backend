package com.example.demo.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setMaxLifetime(1800000);
        config.setPoolName("HospitalHikariPool");

        config.setInitializationFailTimeout(1);

        config.setConnectionTestQuery("SELECT 1");

        log.info("HikariCP configurado con pool máximo de {} conexiones",
            config.getMaximumPoolSize());

        return config;
    }

    @Bean
    public DataSource dataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }
}
