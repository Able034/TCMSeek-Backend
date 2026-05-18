package com.tcmseek.ai.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties({
        AiCacheProperties.class,
        AiPromptProperties.class,
        AiRuntimeProperties.class,
        AiToolProperties.class,
        AiConversationStorageProperties.class,
        TcmReasonProperties.class,
        Neo4jProperties.class
})
public class ServiceConfig {

    @Bean(destroyMethod = "close")
    public Driver neo4jDriver(Neo4jProperties properties) {
        return GraphDatabase.driver(
                properties.getUri(),
                AuthTokens.basic(properties.getUsername(), properties.getPassword()));
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
