package com.tcmseek.tcmseekagentservice.ai.tools;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.tcmseek.tcmseekagentservice.ai.tools.neo4j.Neo4jQueryService;
import com.tcmseek.tcmseekagentservice.ai.tools.neo4j.Neo4jSearchTool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class Neo4jSearchToolIntegrationTest {

    private static Driver driver;
    private static Neo4jSearchTool neo4jSearchTool;

    @BeforeAll
    static void setUp() {
        Properties properties = loadApplicationYaml();
        String uri = properties.getProperty("tcmseek.neo4j.uri");
        String username = properties.getProperty("tcmseek.neo4j.username");
        String password = properties.getProperty("tcmseek.neo4j.password");

        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
        driver.verifyConnectivity();

        Neo4jQueryService neo4jQueryService = new Neo4jQueryService(driver);
        neo4jSearchTool = new Neo4jSearchTool(neo4jQueryService);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.close();
        }
    }

    @Test
    void neo4jSearchQueriesRealNeo4jAndReturnsExistingRows() {
        Sample sample = findExistingSample();

        String result = neo4jSearchTool.neo4jSearch(
                sample.startLabel,
                sample.startName,
                List.of(sample.relationship),
                10
//                1L
        );

        assertThat(JSONUtil.isTypeJSONArray(result)).isTrue();
        JSONArray rows = JSONUtil.parseArray(result);
        assertThat(rows).isNotEmpty();
    }

    @SuppressWarnings("unchecked")
    private static Set<String> allowedValues(String fieldName) {
        try {
            Field field = Neo4jSearchTool.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Set<String>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read " + fieldName, e);
        }
    }

    private static Sample findExistingSample() {
        Set<String> allowedLabels = allowedValues("ALLOWED_LABELS");
        Set<String> allowedRelationships = allowedValues("ALLOWED_RELATIONSHIPS");
        String cypher = ""
                + "MATCH (s)-[r]-(e) "
                + "WHERE s.name IS NOT NULL AND type(r) IN $relationships "
                + "WITH s, r, [label IN labels(s) WHERE label IN $labels] AS matchingLabels "
                + "WHERE size(matchingLabels) > 0 "
                + "RETURN matchingLabels[0] AS startLabel, s.name AS startName, type(r) AS relationship "
                + "LIMIT 1";

        try (Session session = driver.session()) {
            Result result = session.run(cypher, Map.of(
                    "labels", new ArrayList<>(allowedLabels),
                    "relationships", new ArrayList<>(allowedRelationships)
            ));

            assertThat(result.hasNext())
                    .as("Neo4j should contain at least one allowed node and relationship sample")
                    .isTrue();

            org.neo4j.driver.Record record = result.next();
            return new Sample(
                    record.get("startLabel").asString(),
                    record.get("startName").asString(),
                    record.get("relationship").asString()
            );
        }
    }

    private static Properties loadApplicationYaml() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yaml"));
        Properties properties = yaml.getObject();
        if (properties == null) {
            throw new IllegalStateException("Failed to load application.yaml");
        }
        return properties;
    }

    private static final class Sample {

        private final String startLabel;
        private final String startName;
        private final String relationship;

        private Sample(String startLabel, String startName, String relationship) {
            this.startLabel = startLabel;
            this.startName = startName;
            this.relationship = relationship;
        }
    }
}
