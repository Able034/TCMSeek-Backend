package com.tcmseek.tools.semantic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CuratedTopics {

    private CuratedTopics() {
    }

    public static List<SemanticDocument> documents() {
        return List.of(
                topic("anti_inflammation", "抗炎",
                        List.of("炎症", "抗炎症", "anti-inflammatory", "inflammation", "inflammatory response"),
                        List.of("TNF", "IL6", "IL1B", "PTGS2", "NFKB1", "RELA", "CXCL8", "TLR4", "MAPK1", "MAPK3", "JUN", "STAT3"),
                        "抗炎 炎症反应 inflammatory response anti-inflammatory cytokine TNF IL6 IL1B PTGS2 COX2 NF-kappaB TLR4 MAPK"),
                topic("immune_modulation", "免疫调节",
                        List.of("免疫", "调节免疫", "immunomodulatory", "immune response"),
                        List.of("IL6", "TNF", "IL10", "IFNG", "STAT3", "TLR4", "NFKB1", "RELA", "JAK2", "MAPK1"),
                        "免疫调节 immune response immunomodulatory cytokine T cell macrophage IL6 TNF IL10 IFNG JAK STAT NF-kappaB"),
                topic("anti_oxidation", "抗氧化",
                        List.of("抗氧化应激", "氧化应激", "antioxidant", "oxidative stress"),
                        List.of("NFE2L2", "SOD1", "SOD2", "CAT", "GPX1", "HMOX1", "KEAP1", "NOS2", "MAPK1"),
                        "抗氧化 氧化应激 antioxidant oxidative stress ROS Nrf2 KEAP1 SOD CAT GPX HMOX1"),
                topic("hypoglycemic", "降糖",
                        List.of("降血糖", "糖尿病", "diabetes", "hypoglycemic", "glucose metabolism"),
                        List.of("INS", "INSR", "PPARG", "AKT1", "PIK3CA", "SLC2A4", "GCK", "IRS1", "TNF", "IL6"),
                        "降糖 降血糖 糖尿病 diabetes mellitus glucose metabolism insulin resistance INS INSR PPARG AKT1 GLUT4"),
                topic("analgesic", "镇痛",
                        List.of("止痛", "疼痛", "analgesic", "pain relief", "nociception"),
                        List.of("PTGS2", "PTGS1", "TRPV1", "OPRM1", "SCN9A", "TNF", "IL6", "NFKB1"),
                        "镇痛 止痛 疼痛 analgesic pain nociception COX PTGS TRPV1 OPRM1 inflammatory pain"),
                topic("neuroprotection", "神经保护",
                        List.of("保护神经", "神经退行性", "neuroprotective", "neurodegeneration"),
                        List.of("BDNF", "NGF", "AKT1", "MAPK1", "CASP3", "BCL2", "TP53", "APP", "ACHE", "NFE2L2"),
                        "神经保护 neuroprotection neurodegeneration Alzheimer Parkinson BDNF NGF AKT apoptosis oxidative stress"),
                topic("macrophage_inflammation", "巨噬细胞炎症",
                        List.of("巨噬细胞炎症反应", "巨噬细胞炎症通路", "巨噬细胞介导炎症",
                                "macrophage inflammation", "macrophage inflammatory response"),
                        List.of("TNF", "IL6", "IL1B", "TLR4", "NFKB1", "RELA", "MAPK1", "MAPK3",
                                "JUN", "STAT3", "PTGS2", "NOS2", "NLRP3", "CCL2", "CXCL8", "IL10"),
                        "巨噬细胞 炎症反应 macrophage inflammation inflammatory response cytokine TNF IL6 IL1B "
                                + "TLR4 NF-kappaB RELA MAPK JNK STAT3 COX2 PTGS2 iNOS NOS2 NLRP3 CCL2 CXCL8")
        );
    }

    private static SemanticDocument topic(String idSuffix,
                                          String name,
                                          List<String> aliases,
                                          List<String> targets,
                                          String text) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("targetSymbols", targets);
        metadata.put("curated", true);
        metadata.put("scope", "topic_to_target_anchor");
        return new SemanticDocument(
                "topic:" + idSuffix,
                "topic",
                idSuffix,
                null,
                null,
                name,
                aliases,
                "curated_topic",
                idSuffix,
                "Topic: " + name + ". Aliases: " + String.join(", ", aliases)
                        + ". Related target symbols: " + String.join(", ", targets)
                        + ". " + text,
                metadata);
    }
}
