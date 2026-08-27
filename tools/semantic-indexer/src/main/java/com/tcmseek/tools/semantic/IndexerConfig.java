package com.tcmseek.tools.semantic;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record IndexerConfig(
        String mysqlUrl,
        String mysqlUsername,
        String mysqlPassword,
        String pgvectorUrl,
        String pgvectorUsername,
        String pgvectorPassword,
        String embeddingBaseUrl,
        String embeddingApiKey,
        List<String> embeddingApiKeys,
        String embeddingModel,
        int embeddingDimension,
        int limitPerType,
        long sleepMillis,
        int workers,
        boolean dryRun,
        boolean printConfigOnly,
        List<String> types) {

    private static final List<String> DEFAULT_TYPES = List.of(
            "topic",
            "target",
            "disease",
            "herb",
            "prescription",
            "tcm_symptom",
            "wm_symptom",
            "syndrome");

    public static IndexerConfig from(String[] args) {
        Map<String, String> argMap = parseArgs(args);
        Map<String, String> defaults = ProjectConfigDefaults.load();
        List<String> types = parseTypes(value(argMap, "types", "INDEX_TYPES", String.join(",", DEFAULT_TYPES)));
        String embeddingApiKey = value(argMap, "embedding-api-key", "QWEN_EMBEDDING_API_KEY",
                defaultValue(defaults, "embedding-api-key", ""));
        List<String> embeddingApiKeys = parseCsv(
                value(argMap, "embedding-api-keys", "QWEN_EMBEDDING_API_KEYS", embeddingApiKey));
        String mysqlUrl = value(argMap, "mysql-url", "MYSQL_URL", defaultValue(defaults, "mysql-url",
                "jdbc:mysql://127.0.0.1:3306/tcmseek?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"));
        mysqlUrl = setMysqlDatabase(mysqlUrl, value(argMap, "mysql-database", "MYSQL_DATABASE", ""));
        mysqlUrl = addJdbcParamIfMissing(mysqlUrl, "allowPublicKeyRetrieval", "true");
        return new IndexerConfig(
                mysqlUrl,
                value(argMap, "mysql-username", "MYSQL_USERNAME", defaultValue(defaults, "mysql-username", "root")),
                value(argMap, "mysql-password", "MYSQL_PASSWORD", defaultValue(defaults, "mysql-password", "")),
                value(argMap, "pgvector-url", "PGVECTOR_URL",
                        defaultValue(defaults, "pgvector-url", "jdbc:postgresql://127.0.0.1:5434/tcmseek_ai")),
                value(argMap, "pgvector-username", "PGVECTOR_USERNAME",
                        defaultValue(defaults, "pgvector-username", "tcmseek")),
                value(argMap, "pgvector-password", "PGVECTOR_PASSWORD",
                        defaultValue(defaults, "pgvector-password", "tcmseek_dev")),
                trimTrailingSlash(value(argMap, "embedding-base-url", "QWEN_EMBEDDING_BASE_URL",
                        defaultValue(defaults, "embedding-base-url", "https://dashscope.aliyuncs.com/compatible-mode/v1"))),
                embeddingApiKey,
                embeddingApiKeys,
                value(argMap, "embedding-model", "QWEN_EMBEDDING_MODEL",
                        defaultValue(defaults, "embedding-model", "text-embedding-v4")),
                intValue(argMap, "embedding-dimension", "EMBEDDING_DIMENSION", 1024),
                intValue(argMap, "limit", "INDEX_LIMIT", 900000),
                longValue(argMap, "sleep-ms", "INDEX_SLEEP_MS", 0),
                Math.max(1, intValue(argMap, "workers", "INDEX_WORKERS", 1)),
                boolValue(argMap, "dry-run", "INDEX_DRY_RUN", false),
                boolValue(argMap, "print-config-only", "INDEX_PRINT_CONFIG_ONLY", false),
                types);
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        if (args == null) {
            return values;
        }
        for (String arg : args) {
            if (arg == null || arg.isBlank()) {
                continue;
            }
            String normalized = arg.startsWith("--") ? arg.substring(2) : arg;
            int index = normalized.indexOf('=');
            if (index < 0) {
                values.put(normalized, "true");
            } else {
                values.put(normalized.substring(0, index), normalized.substring(index + 1));
            }
        }
        return values;
    }

    private static String value(Map<String, String> argMap, String argName, String envName, String defaultValue) {
        String arg = argMap.get(argName);
        if (arg != null && !arg.isBlank()) {
            return arg;
        }
        String env = System.getenv(envName);
        return env == null || env.isBlank() ? defaultValue : env;
    }

    private static String defaultValue(Map<String, String> defaults, String key, String fallback) {
        String value = defaults.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int intValue(Map<String, String> argMap, String argName, String envName, int defaultValue) {
        String raw = value(argMap, argName, envName, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static long longValue(Map<String, String> argMap, String argName, String envName, long defaultValue) {
        String raw = value(argMap, argName, envName, String.valueOf(defaultValue));
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static boolean boolValue(Map<String, String> argMap, String argName, String envName, boolean defaultValue) {
        String raw = value(argMap, argName, envName, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(raw) || "1".equals(raw) || "yes".equalsIgnoreCase(raw);
    }

    private static List<String> parseTypes(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_TYPES;
        }
        List<String> values = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(DEFAULT_TYPES::contains)
                .distinct()
                .toList();
        return values.isEmpty() ? DEFAULT_TYPES : values;
    }

    private static List<String> parseCsv(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String setMysqlDatabase(String mysqlUrl, String database) {
        if (mysqlUrl == null || mysqlUrl.isBlank() || database == null || database.isBlank()) {
            return mysqlUrl;
        }
        return mysqlUrl.replaceFirst("^(jdbc:mysql://[^/]+/)([^?]*)(.*)$", "$1" + database + "$3");
    }

    private static String addJdbcParamIfMissing(String url, String name, String value) {
        if (url == null || url.isBlank() || url.matches(".*([?&])" + name + "=.*")) {
            return url;
        }
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + name + "=" + value;
    }
}
