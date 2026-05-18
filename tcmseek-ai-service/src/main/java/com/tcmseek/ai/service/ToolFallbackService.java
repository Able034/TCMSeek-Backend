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
        if (!StringUtils.hasText(question)) {
            return false;
        }
        String normalized = normalize(question);

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
                graphTools.findDiseasePrescriptions(diseaseName);
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

        return false;
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
