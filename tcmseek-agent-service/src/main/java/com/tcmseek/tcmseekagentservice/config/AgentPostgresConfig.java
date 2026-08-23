package com.tcmseek.tcmseekagentservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(AgentPostgresProperties.class)
public class AgentPostgresConfig {

    @Bean(destroyMethod = "close")
    public DataSource agentPostgresDataSource(AgentPostgresProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());

        if (StringUtils.hasText(properties.getDriverClassName())) {
            config.setDriverClassName(properties.getDriverClassName());
        }

        AgentPostgresProperties.Hikari hikari = properties.getHikari();
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
    public JdbcTemplate agentConversationJdbcTemplate(
            @Qualifier("agentPostgresDataSource") DataSource agentPostgresDataSource) {
        return new JdbcTemplate(agentPostgresDataSource);
    }

    @Bean
    public DataSourceTransactionManager agentPostgresTransactionManager(
            @Qualifier("agentPostgresDataSource") DataSource agentPostgresDataSource) {
        return new DataSourceTransactionManager(agentPostgresDataSource);
    }
}
