package com.tcmseek.tcmseekagentservice.ai.tools.neo4j;

import org.neo4j.driver.Record;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Neo4j 查询服务
 */
@Component
public class Neo4jQueryService {

    private final Driver neo4jDriver;

    public Neo4jQueryService(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    public List<Map<String, Object>> query(String cypher, String startName, int safeLimit) {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of(
                    "startName", startName,
                    "limit", safeLimit
            ));

            List<Map<String, Object>> rows = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                rows.add(record.asMap(Value::asObject));
            }

            return rows;
        }
    }
}
