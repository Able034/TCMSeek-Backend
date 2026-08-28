package com.tcmseek.tcmseekagentservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(MysqlProperties.class)
public class MysqlConfig {

    @Primary
    @Bean(destroyMethod = "close")
    public DataSource mysqlDataSource(MysqlProperties mysqlProperties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlProperties.getUrl());
        config.setUsername(mysqlProperties.getUsername());
        config.setPassword(mysqlProperties.getPassword());

        if (StringUtils.hasText(mysqlProperties.getDriverClassName())) {
            config.setDriverClassName(mysqlProperties.getDriverClassName());
        }

        MysqlProperties.Hikari hikari = mysqlProperties.getHikari();
        if (hikari.getMaximumPoolSize() != null) {
            config.setMaximumPoolSize(hikari.getMaximumPoolSize());
        }
        if (hikari.getMinimumIdle() != null) {
            config.setMinimumIdle(hikari.getMinimumIdle());
        }
        if (hikari.getConnectionTimeout() != null) {
            config.setConnectionTimeout(hikari.getConnectionTimeout());
        }
        if (hikari.getInitializationFailTimeout() != null) {
            config.setInitializationFailTimeout(hikari.getInitializationFailTimeout());
        }

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) {
        return new JdbcTemplate(mysqlDataSource);
    }
}
