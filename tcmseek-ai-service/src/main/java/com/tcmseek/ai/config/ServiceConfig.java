package com.tcmseek.ai.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AiCacheProperties.class,
        AiPromptProperties.class,
        AiRuntimeProperties.class,
        AiToolProperties.class,
        AiConversationStorageProperties.class,
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
    public java.util.concurrent.ExecutorService aiModelExecutor(AiRuntimeProperties properties) {
        int poolSize = Math.max(1, properties.getModelExecutorPoolSize());
        return java.util.concurrent.Executors.newFixedThreadPool(poolSize);
    }
}
