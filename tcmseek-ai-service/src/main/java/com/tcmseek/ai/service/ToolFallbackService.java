package com.tcmseek.ai.service;

import com.tcmseek.ai.tools.TcmGraphTools;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ToolFallbackService {

    private static final List<String> QUERY_WORDS = Arrays.asList("查询", "查看", "列出", "找出", "检索", "搜索");

    private final TcmGraphTools graphTools;

    public ToolFallbackService(TcmGraphTools graphTools) {
        this.graphTools = graphTools;
    }

    public boolean tryExecute(String question) {
        return tryExecute(question, true);
    }

    public boolean tryExecute(String question, boolean semanticFallbackEnabled) {
        if (!StringUtils.hasText(question)) {
            return false;
        }
        String normalized = normalize(question);

        String semanticTopic = extractSemanticTopic(normalized);
        if (semanticFallbackEnabled && StringUtils.hasText(semanticTopic) && looksLikeCompoundTargetQuestion(normalized)) {
            String herbName = extractHerbAfterSemanticTopic(normalized);
            if (StringUtils.hasText(herbName) && !looksLikePrescription(herbName)) {
                graphTools.findHerbCompoundTargetsByTopic(herbName, semanticTopic);
                return true;
            }
        }

        if (looksLikeHerbPrescriptionConditionQuestion(normalized)) {
            String herbName = extractHerbForPrescriptionCondition(normalized);
            String conditionName = extractConditionForPrescriptionCondition(normalized);
            if (StringUtils.hasText(herbName)
                    && !looksLikeQuestionOnlySubject(herbName)
                    && StringUtils.hasText(conditionName)) {
                graphTools.findHerbDiseasePrescriptions(herbName, conditionName);
                return true;
            }
        }

        if (containsAny(normalized, Arrays.asList("共同靶点", "共同基因", "共同作用靶点"))) {
            List<String> herbs = extractEntitiesBefore(normalized, firstMatched(normalized, Arrays.asList("共同作用靶点", "共同靶点", "共同基因")));
            if (herbs.size() >= 2) {
                graphTools.findCommonTargets(String.join("，", herbs));
                return true;
            }
        }

        if (containsAny(normalized, Arrays.asList("共同化合物", "共同的化合物", "共有化合物", "共有的化合物",
                "相同化合物", "相同的化合物", "共同成分", "共同的成分", "共有成分", "共有的成分",
                "相同成分", "相同的成分", "共同活性成分", "共同的活性成分", "共同有效成分",
                "共同的有效成分", "共同物"))) {
            String keyword = firstMatched(normalized, Arrays.asList("共同的活性成分", "共同活性成分",
                    "共同的有效成分", "共同有效成分",
                    "共同的化合物", "共同化合物", "共有的化合物", "共有化合物",
                    "相同的化合物", "相同化合物", "共同的成分", "共同成分",
                    "共有的成分", "共有成分", "相同的成分", "相同成分", "共同物"));
            List<String> herbs = extractEntitiesBefore(normalized, keyword);
            if (herbs.size() >= 2) {
                graphTools.findCommonCompounds(String.join("，", herbs));
                return true;
            }
        }

        if (containsAny(normalized, Arrays.asList("化合物", "活性成分", "有效成分", "成分"))
                && containsAny(normalized, Arrays.asList("靶标", "靶点", "靶向", "target", "Target", "基因"))) {
            String herbName = extractSingleBeforeAny(normalized, Arrays.asList("包含哪些化合物", "有哪些化合物", "含有哪些化合物",
                    "包含", "含有", "有哪些活性成分", "有哪些有效成分", "活性成分", "有效成分", "化合物", "成分"));
            if (!StringUtils.hasText(herbName)) {
                herbName = extractSingleBeforeAny(normalized, Arrays.asList("作用哪些靶标", "作用哪些靶点", "对应哪些靶标",
                        "对应哪些靶点", "关联哪些靶标", "关联哪些靶点", "靶标", "靶点", "靶向", "target", "Target", "基因"));
            }
            if (StringUtils.hasText(herbName) && !looksLikePrescription(herbName)) {
                graphTools.findHerbCompoundTargets(herbName);
                return true;
            }
        }

        if (containsAny(normalized, Arrays.asList("化合物", "活性成分", "成分"))) {
            String herbName = extractSingleBeforeAny(normalized, Arrays.asList("包含哪些化合物", "有哪些化合物", "含有哪些化合物",
                    "包含", "含有", "有哪些活性成分", "活性成分", "化合物", "成分"));
            if (StringUtils.hasText(herbName) && !looksLikePrescription(herbName)) {
                graphTools.findHerbCompounds(herbName);
                return true;
            }
        }

        if ((normalized.contains("疾病") || normalized.contains("病"))
                && containsAny(normalized, Arrays.asList("靶点", "基因", "target", "Target"))) {
            String diseaseName = extractSingleBeforeAny(normalized, Arrays.asList("关联哪些靶点", "相关靶点", "有哪些靶点", "有什么靶点",
                    "关联哪些基因", "相关基因", "有哪些基因", "有什么基因", "关联", "相关", "靶点", "基因"));
            if (StringUtils.hasText(diseaseName)) {
                graphTools.findDiseaseTargets(diseaseName);
                return true;
            }
        }

        if ((normalized.contains("疾病") || normalized.contains("病"))
                && (normalized.contains("相关中药") || normalized.contains("哪些中药")
                || normalized.contains("中药有哪些") || normalized.contains("治疗中药")
                || (normalized.contains("中药") && containsAny(normalized, Arrays.asList("治疗", "治"))))) {
            String diseaseName = extractSingleBeforeAny(normalized, Arrays.asList("有哪些相关中药", "有哪些中药", "相关中药", "治疗中药", "中药"));
            if (!StringUtils.hasText(diseaseName) || diseaseName.startsWith("治疗") || diseaseName.startsWith("治")) {
                diseaseName = extractAfterAnyBeforeAny(normalized,
                        Arrays.asList("可以治疗", "能治疗", "治疗", "治"),
                        Arrays.asList("的相关中药", "的中药", "相关中药", "中药", "有哪些", "有哪"));
            }
            if (StringUtils.hasText(diseaseName)) {
                graphTools.findDiseaseHerbs(diseaseName);
                return true;
            }
        }

        if ((normalized.contains("疾病") || normalized.contains("病"))
                && (normalized.contains("相关方剂") || normalized.contains("哪些方剂")
                || normalized.contains("方剂有哪些") || normalized.contains("治疗方剂")
                || (normalized.contains("方剂") && containsAny(normalized, Arrays.asList("治疗", "治"))))) {
            String diseaseName = extractSingleBeforeAny(normalized, Arrays.asList("有哪些相关方剂", "有哪些方剂", "相关方剂", "治疗方剂", "方剂"));
            if (!StringUtils.hasText(diseaseName) || diseaseName.startsWith("治疗") || diseaseName.startsWith("治")) {
                diseaseName = extractAfterAnyBeforeAny(normalized,
                        Arrays.asList("可以治疗", "能治疗", "治疗", "治"),
                        Arrays.asList("的相关方剂", "的方剂", "相关方剂", "方剂", "有哪些", "有哪"));
            }
            if (StringUtils.hasText(diseaseName)) {
                if (semanticFallbackEnabled) {
                    graphTools.findDiseasePrescriptions(diseaseName);
                } else {
                    graphTools.findDiseasePrescriptionsExact(diseaseName);
                }
                return true;
            }
        }

        if (containsAny(normalized, Arrays.asList("组成", "配伍", "有哪些药", "有哪些中药", "含哪些药", "包括哪些药", "由什么组成"))) {
            String prescriptionName = extractSingleBeforeAny(normalized, Arrays.asList("由什么组成", "有哪些中药", "有哪些药",
                    "含哪些药", "包括哪些药", "组成", "配伍"));
            if (StringUtils.hasText(prescriptionName) && looksLikePrescription(prescriptionName)) {
                graphTools.findPrescriptionHerbs(prescriptionName);
                return true;
            }
        }

        if (containsAny(normalized, Arrays.asList("可以治什么病", "能治什么病", "治什么病", "治疗什么病", "治啥",
                "主治什么", "主治", "功效", "适应症", "适应证", "作用", "用途", "有什么用", "干什么", "干嘛", "补什么"))) {
            String subject = extractSingleBeforeAny(normalized, Arrays.asList("可以治什么病", "能治什么病", "治什么病", "治疗什么病",
                    "治啥", "主治什么", "主治", "有什么功效", "有何功效", "功效", "适应症", "适应证",
                    "有什么作用", "有什么用", "有何作用", "作用", "用途", "干什么", "干嘛", "补什么"));
            if (StringUtils.hasText(subject)) {
                if (looksLikePrescription(subject)) {
                    graphTools.findPrescriptionClinicalUse(subject);
                } else {
                    graphTools.findHerbClinicalUse(subject);
                }
                return true;
            }
        }

        if (normalized.contains("治疗哪些疾病") || normalized.contains("关联哪些疾病")) {
            String herbName = extractSingleBeforeAny(normalized, Arrays.asList("治疗哪些疾病", "关联哪些疾病", "疾病"));
            if (StringUtils.hasText(herbName)) {
                graphTools.findHerbDiseases(herbName);
                return true;
            }
        }

        if (normalized.contains("证候") && normalized.contains("症状")) {
            String syndromeName = extractSingleBeforeAny(normalized, Arrays.asList("包含", "关联", "症状"));
            if (StringUtils.hasText(syndromeName)) {
                graphTools.findSyndromeSymptoms(syndromeName);
                return true;
            }
        }

        if (normalized.contains("方剂") && normalized.contains("症状")) {
            String prescriptionName = extractSingleBeforeAny(normalized, Arrays.asList("治疗", "关联", "症状"));
            if (StringUtils.hasText(prescriptionName)) {
                graphTools.findPrescriptionSymptoms(prescriptionName);
                return true;
            }
        }

        if (normalized.contains("方剂") && normalized.contains("证候")) {
            String prescriptionName = extractSingleBeforeAny(normalized, Arrays.asList("治疗", "关联", "证候"));
            if (StringUtils.hasText(prescriptionName)) {
                graphTools.findPrescriptionSyndromes(prescriptionName);
                return true;
            }
        }

        if (normalized.contains("方剂") || normalized.contains("中药组成") || normalized.contains("包含哪些中药")) {
            String prescriptionName = extractSingleBeforeAny(normalized, Arrays.asList("包含", "组成", "有哪些中药", "中药"));
            if (StringUtils.hasText(prescriptionName)) {
                graphTools.findPrescriptionHerbs(prescriptionName);
                return true;
            }
        }

        if (looksLikeBroadTcmQuestion(normalized)) {
            if (semanticFallbackEnabled) {
                graphTools.findBySemanticIntent(question);
            } else {
                graphTools.findByGraphIntentExact(question);
            }
            return true;
        }

        return false;
    }

    private boolean looksLikeCompoundTargetQuestion(String text) {
        return containsAny(text, Arrays.asList(
                "\u5316\u5408\u7269",
                "\u6210\u5206",
                "\u6d3b\u6027\u6210\u5206",
                "\u6709\u6548\u6210\u5206"))
                && containsAny(text, Arrays.asList(
                "\u9776\u6807",
                "\u9776\u70b9",
                "\u57fa\u56e0",
                "target",
                "Target"));
    }

    private boolean looksLikeHerbPrescriptionConditionQuestion(String text) {
        return containsAny(text, Arrays.asList(
                "\u65b9\u5242",
                "\u65b9\u5b50",
                "\u836f\u65b9",
                "\u5904\u65b9"))
                && containsAny(text, Arrays.asList(
                "\u6cbb\u7597",
                "\u53ef\u4ee5\u6cbb",
                "\u80fd\u6cbb",
                "\u4e3b\u6cbb",
                "\u6cbb"));
    }

    private String extractHerbForPrescriptionCondition(String text) {
        String herbName = extractSingleBeforeAny(text, Arrays.asList(
                "\u7684\u65b9\u5242",
                "\u7684\u65b9\u5b50",
                "\u7684\u836f\u65b9",
                "\u7684\u5904\u65b9",
                "\u65b9\u5242",
                "\u65b9\u5b50",
                "\u836f\u65b9",
                "\u5904\u65b9"));
        if (StringUtils.hasText(herbName)) {
            int lastDe = herbName.lastIndexOf("\u7684");
            if (lastDe >= 0 && lastDe < herbName.length() - 1) {
                herbName = herbName.substring(lastDe + 1);
            }
        }
        return cleanEntityText(herbName);
    }

    private String extractConditionForPrescriptionCondition(String text) {
        return extractAfterAnyBeforeAny(text,
                Arrays.asList(
                        "\u53ef\u4ee5\u6cbb\u7597",
                        "\u53ef\u4ee5\u6cbb",
                        "\u80fd\u6cbb\u7597",
                        "\u80fd\u6cbb",
                        "\u4e3b\u6cbb",
                        "\u6cbb\u7597",
                        "\u6cbb"),
                Arrays.asList(
                        "\u7684\u76f8\u5173\u65b9\u5242",
                        "\u7684\u65b9\u5242",
                        "\u7684\u65b9\u5b50",
                        "\u7684\u836f\u65b9",
                        "\u7684\u5904\u65b9",
                        "\u76f8\u5173\u65b9\u5242",
                        "\u65b9\u5242",
                        "\u65b9\u5b50",
                        "\u836f\u65b9",
                        "\u5904\u65b9",
                        "\u6709\u54ea\u4e9b",
                        "\u6709\u54ea"));
    }

    private boolean looksLikeQuestionOnlySubject(String text) {
        if (!StringUtils.hasText(text)) {
            return true;
        }
        String cleaned = text.trim();
        return cleaned.length() > 20
                || containsAny(cleaned, Arrays.asList(
                "\u54ea\u4e9b",
                "\u4ec0\u4e48",
                "\u600e\u4e48",
                "\u5982\u4f55",
                "\u6709\u6ca1\u6709",
                "\u53ef\u4ee5",
                "\u80fd\u4e0d\u80fd"));
    }

    private boolean looksLikeBroadTcmQuestion(String text) {
        return containsAny(text, Arrays.asList(
                "\u65b9\u5242",
                "\u65b9\u5b50",
                "\u836f\u65b9",
                "\u5904\u65b9",
                "\u4e2d\u836f",
                "\u4e2d\u533b",
                "\u4e2d\u533b\u836f",
                "\u836f\u6750",
                "\u8349\u836f",
                "\u75be\u75c5",
                "\u75c7\u72b6",
                "\u8bc1\u5019",
                "\u8bc1\u578b",
                "\u6cbb\u7597",
                "\u8c03\u7406",
                "\u7f13\u89e3",
                "\u6539\u5584",
                "\u4e3b\u6cbb",
                "\u9002\u7528"));
    }

    private String extractSemanticTopic(String text) {
        int index = text.indexOf("\u76f8\u5173");
        if (index <= 0) {
            return null;
        }
        return cleanEntityText(text.substring(0, index));
    }

    private String extractHerbAfterSemanticTopic(String text) {
        int index = text.indexOf("\u76f8\u5173");
        if (index < 0) {
            return null;
        }
        String tail = text.substring(index + "\u76f8\u5173".length());
        if (tail.startsWith("\u7684") || tail.startsWith("\u4e8e")) {
            tail = tail.substring(1);
        }
        int end = firstIndexOfAny(tail, Arrays.asList(
                "\u6d3b\u6027\u6210\u5206",
                "\u6709\u6548\u6210\u5206",
                "\u5316\u5408\u7269",
                "\u6210\u5206",
                "\u9776\u6807",
                "\u9776\u70b9",
                "\u57fa\u56e0"));
        if (end > 0) {
            tail = tail.substring(0, end);
        }
        return cleanEntityText(tail);
    }

    private int firstIndexOfAny(String text, List<String> keywords) {
        int index = -1;
        for (String keyword : keywords) {
            int candidate = text.indexOf(keyword);
            if (candidate >= 0 && (index < 0 || candidate < index)) {
                index = candidate;
            }
        }
        return index;
    }

    private String firstMatched(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return keyword;
            }
        }
        return keywords.isEmpty() ? "" : keywords.get(0);
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikePrescription(String subject) {
        return containsAny(subject, Arrays.asList("方", "丸", "散", "汤", "膏", "丹", "胶囊", "颗粒", "片", "口服液", "饮", "剂"));
    }

    private List<String> extractEntitiesBefore(String text, String keyword) {
        int index = text.indexOf(keyword);
        if (index <= 0) {
            return List.of();
        }
        String prefix = afterLastQueryWord(text.substring(0, index));
        prefix = cleanEntityText(prefix);
        String[] parts = Pattern.compile("以及|和|与|及|跟|、|,|，|;|；|/").split(prefix);
        List<String> entities = new ArrayList<>();
        for (String part : parts) {
            String entity = cleanEntityText(part);
            if (StringUtils.hasText(entity)) {
                entities.add(entity);
            }
        }
        return entities.size() >= 2 ? entities : List.of();
    }

    private String extractSingleBeforeAny(String text, List<String> keywords) {
        int index = -1;
        for (String keyword : keywords) {
            int candidate = text.indexOf(keyword);
            if (candidate > 0 && (index < 0 || candidate < index)) {
                index = candidate;
            }
        }
        if (index <= 0) {
            return null;
        }
        return cleanEntityText(afterLastQueryWord(text.substring(0, index)));
    }

    private String extractAfterAnyBeforeAny(String text, List<String> markers, List<String> stopWords) {
        int start = -1;
        int markerLength = 0;
        for (String marker : markers) {
            int candidate = text.lastIndexOf(marker);
            if (candidate >= 0 && (candidate > start || (candidate == start && marker.length() > markerLength))) {
                start = candidate;
                markerLength = marker.length();
            }
        }
        if (start < 0) {
            return null;
        }
        String tail = text.substring(start + markerLength);
        int end = tail.length();
        for (String stopWord : stopWords) {
            int candidate = tail.indexOf(stopWord);
            if (candidate >= 0 && candidate < end) {
                end = candidate;
            }
        }
        return cleanEntityText(tail.substring(0, end));
    }

    private String afterLastQueryWord(String text) {
        String result = text;
        for (String word : QUERY_WORDS) {
            int index = result.lastIndexOf(word);
            if (index >= 0) {
                result = result.substring(index + word.length());
            }
        }
        return result;
    }

    private String cleanEntityText(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text
                .replaceAll("请调用.*?工具", "")
                .replaceAll("只根据.*", "")
                .replaceAll("^(方剂|中药|疾病|证候|化合物|通路|医案)", "")
                .replace("有哪些", "")
                .replace("有什么", "")
                .replace("有何", "")
                .replace("的", "")
                .replace("哪些", "")
                .replace("什么", "")
                .replace("啥", "")
                .replace("可以", "")
                .replace("能够", "")
                .replace("能", "")
                .replace("有关", "")
                .replace("相关", "")
                .replace("一下", "")
                .trim();
        cleaned = cleaned.replaceAll("^[：:，,。\\s]+", "");
        cleaned = cleaned.replaceAll("[：:，,。？?\\s]+$", "");
        return cleaned;
    }

    private String normalize(String question) {
        return question == null ? "" : question.replaceAll("\\s+", "");
    }
}
