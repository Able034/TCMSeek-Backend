package com.tcmseek.ai.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties({
        AiCacheProperties.class,
        AiPromptProperties.class,
        AiRuntimeProperties.class,
        AiToolProperties.class,
        AiConversationStorageProperties.class,
        TcmReasonProperties.class,
        ShowModelChatProperties.class,
        AiVectorProperties.class,
        Neo4jProperties.class
})
public class ServiceConfig {

    @Bean(destroyMethod = "close")
    public Driver neo4jDriver(Neo4jProperties properties) {
        return GraphDatabase.driver(
                properties.getUri(),
                AuthTokens.basic(properties.getUsername(), properties.getPassword()));
    }

    @Bean(name = "vectorDataSource", destroyMethod = "close")
    @ConditionalOnProperty(prefix = "tcmseek.ai.vector", name = "enabled", havingValue = "true")
    public DataSource vectorDataSource(AiVectorProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setMaximumPoolSize(Math.max(1, properties.getMaximumPoolSize()));
        dataSource.setPoolName("tcmseek-vector-pool");
        return dataSource;
    }

    @Bean(name = "vectorJdbcTemplate")
    @ConditionalOnProperty(prefix = "tcmseek.ai.vector", name = "enabled", havingValue = "true")
    public JdbcTemplate vectorJdbcTemplate(@Qualifier("vectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(destroyMethod = "shutdown")
    @Primary
    public java.util.concurrent.ExecutorService aiModelExecutor(AiRuntimeProperties properties) {
        int poolSize = Math.max(1, properties.getModelExecutorPoolSize());
        return java.util.concurrent.Executors.newFixedThreadPool(poolSize);
    }

    @Bean(destroyMethod = "shutdown")
    public java.util.concurrent.ScheduledExecutorService aiStreamHeartbeatExecutor() {
        return java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
    }
}
