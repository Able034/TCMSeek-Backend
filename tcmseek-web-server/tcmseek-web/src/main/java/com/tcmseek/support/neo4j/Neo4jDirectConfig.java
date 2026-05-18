package com.tcmseek.support.neo4j;

import lombok.Data;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j直连配置 - 用于TCM专业模式问答
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tcm.neo4j")
public class Neo4jDirectConfig {

    /**
     * Neo4j URI
     */
    private String uri = "bolt://localhost:7687";

    /**
     * 用户名
     */
    private String username = "neo4j";

    /**
     * 密码
     */
    private String password = "";

    /**
     * 创建Neo4j Driver Bean
     */
    @Bean(destroyMethod = "close")
    public Driver neo4jDriver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }
}
