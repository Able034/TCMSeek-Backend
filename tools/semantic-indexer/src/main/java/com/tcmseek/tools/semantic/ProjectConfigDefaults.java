package com.tcmseek.tools.semantic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ProjectConfigDefaults {

    private static final String DEFAULT_MYSQL_URL =
            "jdbc:mysql://192.168.245.128:3306/ruoyi_vue_demo?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8";
    private static final String DEFAULT_PGVECTOR_URL =
            "jdbc:postgresql://192.168.245.128:5434/tcmseek_ai";
    private static final String DEFAULT_EMBEDDING_BASE_URL =
            "http://127.0.0.1:8001/v1";
    private static final String DEFAULT_EMBEDDING_MODEL =
            "BAAI/bge-m3";

    private ProjectConfigDefaults() {
    }

    public static Map<String, String> load() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("mysql-url", DEFAULT_MYSQL_URL);
        defaults.put("mysql-username", "root");
        defaults.put("mysql-password", "123456");
        defaults.put("pgvector-url", DEFAULT_PGVECTOR_URL);
        defaults.put("pgvector-username", "tcmseek");
        defaults.put("pgvector-password", "tcmseek_dev");
        defaults.put("embedding-base-url", DEFAULT_EMBEDDING_BASE_URL);
        defaults.put("embedding-api-key", "local");
        defaults.put("embedding-model", DEFAULT_EMBEDDING_MODEL);

        Optional<Path> repoRoot = findRepoRoot();
        if (repoRoot.isEmpty()) {
            return defaults;
        }

        readMysqlDefaults(repoRoot.get()
                .resolve("tcmseek-web-server/tcmseek-admin/src/main/resources/application-druid.yml"), defaults);
        readVectorDefaults(repoRoot.get()
                .resolve("tcmseek-ai-service/src/main/resources/application.yml"), defaults);
        return defaults;
    }

    private static Optional<Path> findRepoRoot() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("tcmseek-ai-service"))
                    && Files.exists(current.resolve("tcmseek-web-server"))) {
                return Optional.of(current);
            }
            current = current.getParent();
        }
        return Optional.empty();
    }

    private static void readMysqlDefaults(Path configPath, Map<String, String> defaults) {
        if (!Files.exists(configPath)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(configPath);
            for (int i = 0; i < lines.size(); i++) {
                KeyValue keyValue = parseKeyValue(lines.get(i));
                if (keyValue == null || !"url".equals(keyValue.key())) {
                    continue;
                }
                String url = expandConfigValue(keyValue.value());
                if (!url.startsWith("jdbc:mysql:")) {
                    continue;
                }
                defaults.put("mysql-url", url);
                for (int j = i + 1; j < Math.min(lines.size(), i + 10); j++) {
                    KeyValue nearby = parseKeyValue(lines.get(j));
                    if (nearby == null) {
                        continue;
                    }
                    if ("username".equals(nearby.key())) {
                        defaults.put("mysql-username", expandConfigValue(nearby.value()));
                    }
                    if ("password".equals(nearby.key())) {
                        defaults.put("mysql-password", expandConfigValue(nearby.value()));
                        break;
                    }
                }
                return;
            }
        } catch (IOException ignored) {
            // Keep hard-coded development defaults when local config cannot be read.
        }
    }

    private static void readVectorDefaults(Path configPath, Map<String, String> defaults) {
        if (!Files.exists(configPath)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(configPath);
            boolean insideVector = false;
            for (String line : lines) {
                if (!insideVector) {
                    if (line.matches("^\\s*vector:\\s*$")) {
                        insideVector = true;
                    }
                    continue;
                }
                if (line.matches("^\\s*tools:\\s*$")) {
                    break;
                }

                KeyValue keyValue = parseKeyValue(line);
                if (keyValue == null) {
                    continue;
                }
                String value = expandConfigValue(keyValue.value());
                switch (keyValue.key()) {
                    case "url" -> defaults.put("pgvector-url", value);
                    case "username" -> defaults.put("pgvector-username", value);
                    case "password" -> defaults.put("pgvector-password", value);
                    default -> {
                    }
                }
            }
        } catch (IOException ignored) {
            // Keep hard-coded development defaults when local config cannot be read.
        }
    }

    private static KeyValue parseKeyValue(String line) {
        if (line == null) {
            return null;
        }
        String withoutComment = line.replaceFirst("\\s+#.*$", "").trim();
        int index = withoutComment.indexOf(':');
        if (index < 0) {
            return null;
        }
        String key = withoutComment.substring(0, index).trim();
        String value = withoutComment.substring(index + 1).trim();
        if (key.isBlank()) {
            return null;
        }
        return new KeyValue(key, trimQuotes(value));
    }

    private static String expandConfigValue(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = trimQuotes(value.trim());
        if (trimmed.matches("^\\$\\{[^:}]+:.*}$")) {
            int colon = trimmed.indexOf(':');
            return trimmed.substring(colon + 1, trimmed.length() - 1);
        }
        if (trimmed.matches("^\\$\\{[^}]+}$")) {
            return "";
        }
        return trimmed;
    }

    private static String trimQuotes(String value) {
        String result = value == null ? "" : value.trim();
        if ((result.startsWith("\"") && result.endsWith("\""))
                || (result.startsWith("'") && result.endsWith("'"))) {
            return result.substring(1, result.length() - 1);
        }
        return result;
    }

    private record KeyValue(String key, String value) {
    }
}
