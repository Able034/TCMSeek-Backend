package com.tcmseek.tcmseekagentservice.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        Neo4jProperties.class
})
public class ServiceConfig {


    @Bean(destroyMethod = "close")
    public Driver neo4jDriver(Neo4jProperties neo4jProperties) {
        return GraphDatabase.driver(
                neo4jProperties.getUri(),
                AuthTokens.basic(neo4jProperties.getUsername(), neo4jProperties.getPassword())
        );
    }


}
