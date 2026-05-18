package com.tcmseek.ai.graph;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TcmGraphRepository {

    private static final Logger log = LoggerFactory.getLogger(TcmGraphRepository.class);

    private final Driver driver;

    public TcmGraphRepository(Driver driver) {
        this.driver = driver;
    }

    public List<Map<String, Object>> query(String cypher, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Session session = driver.session()) {
            Result result = session.run(cypher, params);
            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> row = new HashMap<>();
                for (String key : record.keys()) {
                    row.put(key, toPlainValue(record.get(key)));
                }
                rows.add(row);
            }
            log.debug("neo4j query completed rows={} costMs={} paramKeys={}",
                    rows.size(), System.currentTimeMillis() - start, params == null ? List.of() : params.keySet());
        }
        return rows;
    }

    private Object toPlainValue(Value value) {
        if (value == null || value.isNull()) {
            return null;
        }
        String typeName = value.type().name();
        if ("NODE".equals(typeName)) {
            Map<String, Object> node = new HashMap<>(value.asNode().asMap());
            List<String> labels = new ArrayList<>();
            value.asNode().labels().forEach(labels::add);
            node.put("_labels", labels);
            return node;
        }
        if ("LIST".equals(typeName)) {
            List<Object> values = new ArrayList<>();
            for (Value item : value.values()) {
                values.add(toPlainValue(item));
            }
            return values;
        }
        return value.asObject();
    }
}
