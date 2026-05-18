package com.tcmseek.ai.health;

import com.tcmseek.ai.config.Neo4jProperties;
import org.neo4j.driver.Driver;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class Neo4jHealthIndicator implements HealthIndicator {

    private final Driver driver;

    private final Neo4jProperties properties;

    public Neo4jHealthIndicator(Driver driver, Neo4jProperties properties) {
        this.driver = driver;
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            driver.verifyConnectivity();
            return Health.up()
                    .withDetail("uri", properties.getUri())
                    .withDetail("username", properties.getUsername())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("uri", properties.getUri())
                    .withDetail("username", properties.getUsername())
                    .build();
        }
    }
}
