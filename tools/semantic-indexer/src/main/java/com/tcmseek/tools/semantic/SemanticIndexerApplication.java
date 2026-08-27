package com.tcmseek.tools.semantic;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class SemanticIndexerApplication {

    public static void main(String[] args) throws Exception {
        IndexerConfig config = IndexerConfig.from(args);
        System.out.println("semantic-indexer starting");
        System.out.println("types=" + config.types());
        System.out.println("limitPerType=" + config.limitPerType());
        System.out.println("workers=" + config.workers());
        System.out.println("sleepMillis=" + config.sleepMillis());
        System.out.println("dryRun=" + config.dryRun());
        System.out.println("mysqlUrl=" + config.mysqlUrl());
        System.out.println("mysqlUsername=" + config.mysqlUsername());
        System.out.println("mysqlPassword=" + mask(config.mysqlPassword()));
        System.out.println("pgvectorUrl=" + config.pgvectorUrl());
        System.out.println("pgvectorUsername=" + config.pgvectorUsername());
        System.out.println("pgvectorPassword=" + mask(config.pgvectorPassword()));
        System.out.println("embeddingBaseUrl=" + config.embeddingBaseUrl());
        System.out.println("embeddingModel=" + config.embeddingModel());
        System.out.println("embeddingApiKey=" + mask(config.embeddingApiKey()));
        System.out.println("embeddingApiKeys=" + config.embeddingApiKeys().size());

        if (config.printConfigOnly()) {
            return;
        }

        try (PgvectorWriter writer = new PgvectorWriter(config)) {
            EmbeddingClient embeddingClient = new EmbeddingClient(config);
            writer.initializeSchema();

            int requested = 0;
            AtomicInteger indexed = new AtomicInteger();
            MysqlEntityReader reader = null;
            ExecutorService executor = Executors.newFixedThreadPool(config.workers());
            try {
                for (String type : config.types()) {
                    List<SemanticDocument> documents;
                    if ("topic".equals(type)) {
                        documents = CuratedTopics.documents();
                    } else {
                        if (reader == null) {
                            reader = new MysqlEntityReader(config);
                        }
                        documents = reader.read(type, config.limitPerType());
                    }
                    requested += documents.size();
                    System.out.println("type=" + type + " fetched=" + documents.size());

                    if (config.dryRun()) {
                        printDryRun(documents);
                    } else {
                        processDocuments(config, embeddingClient, writer, executor, documents, indexed);
                    }
                }
            } finally {
                executor.shutdownNow();
                if (reader != null) {
                    reader.close();
                }
            }

            System.out.println("semantic-indexer completed requested=" + requested + " indexed=" + indexed.get());
        }
    }

    private static void printDryRun(List<SemanticDocument> documents) {
        for (SemanticDocument document : documents) {
            System.out.println("dry-run " + document.id() + " name=" + document.name());
        }
    }

    private static void processDocuments(IndexerConfig config,
                                         EmbeddingClient embeddingClient,
                                         PgvectorWriter writer,
                                         ExecutorService executor,
                                         List<SemanticDocument> documents,
                                         AtomicInteger indexed) throws Exception {
        ExecutorCompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        int maxInFlight = Math.max(config.workers() * 20, config.workers());
        int submitted = 0;
        int completed = 0;
        int next = 0;

        while (next < documents.size() || completed < submitted) {
            while (next < documents.size() && submitted - completed < maxInFlight) {
                SemanticDocument document = documents.get(next++);
                completionService.submit(task(config, embeddingClient, writer, document, indexed));
                submitted++;
            }

            Future<Void> future = completionService.take();
            future.get();
            completed++;
        }
    }

    private static Callable<Void> task(IndexerConfig config,
                                       EmbeddingClient embeddingClient,
                                       PgvectorWriter writer,
                                       SemanticDocument document,
                                       AtomicInteger indexed) {
        return () -> {
            String vectorLiteral = embeddingClient.embedAsVectorLiteral(document.textForEmbedding());
            writer.upsert(document, vectorLiteral);
            int current = indexed.incrementAndGet();
            if (current % 100 == 0) {
                System.out.println("indexed=" + current);
            }
            if (config.sleepMillis() > 0) {
                Thread.sleep(config.sleepMillis());
            }
            return null;
        };
    }

    private static String mask(String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }
}
