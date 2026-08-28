from __future__ import annotations

import re

from app.tools import TcmGraphTools


class ToolFallbackRouter:
    QUERY_WORDS = ["查询", "查看", "列出", "找出", "检索", "搜索"]

    def __init__(self, graph_tools: TcmGraphTools) -> None:
        self._graph_tools = graph_tools

    def try_execute(self, question: str | None) -> bool:
        if not question or not question.strip():
            return False
        normalized = re.sub(r"\s+", "", question)

        semantic_topic = self._extract_semantic_topic(normalized)
        if semantic_topic and self._looks_like_compound_target_question(normalized):
            herb_name = self._extract_herb_after_semantic_topic(normalized)
            if herb_name and not self._looks_like_prescription(herb_name):
                self._graph_tools.find_herb_compound_targets_by_topic(herb_name, semantic_topic)
                return True

        if self._contains_any(normalized, ["共同靶点", "共同基因", "共同作用靶点"]):
            keyword = self._first_matched(normalized, ["共同作用靶点", "共同靶点", "共同基因"])
            herbs = self._extract_entities_before(normalized, keyword)
            if len(herbs) >= 2:
                self._graph_tools.find_common_targets("，".join(herbs))
                return True

        if self._contains_any(
            normalized,
            [
                "共同化合物",
                "共同的化合物",
                "共有化合物",
                "相同化合物",
                "共同成分",
                "共有成分",
                "相同成分",
                "共同活性成分",
                "共同有效成分",
            ],
        ):
            keyword = self._first_matched(normalized, ["共同活性成分", "共同有效成分", "共同化合物", "共有化合物", "相同化合物", "共同成分"])
            herbs = self._extract_entities_before(normalized, keyword)
            if len(herbs) >= 2:
                self._graph_tools.find_common_compounds("，".join(herbs))
                return True

        if self._contains_any(normalized, ["化合物", "活性成分", "有效成分", "成分"]) and self._contains_any(
            normalized,
            ["靶标", "靶点", "靶向", "target", "Target", "基因"],
        ):
            herb_name = self._extract_single_before_any(
                normalized,
                ["包含哪些化合物", "有哪些化合物", "含有哪些化合物", "包含", "含有", "有哪些活性成分", "有哪些有效成分", "活性成分", "有效成分", "化合物", "成分"],
            )
            if not herb_name:
                herb_name = self._extract_single_before_any(
                    normalized,
                    ["作用哪些靶标", "作用哪些靶点", "对应哪些靶标", "对应哪些靶点", "关联哪些靶标", "关联哪些靶点", "靶标", "靶点", "靶向", "target", "Target", "基因"],
                )
            if herb_name and not self._looks_like_prescription(herb_name):
                self._graph_tools.find_herb_compound_targets(herb_name)
                return True

        if self._contains_any(normalized, ["化合物", "活性成分", "成分"]):
            herb_name = self._extract_single_before_any(
                normalized,
                ["包含哪些化合物", "有哪些化合物", "含有哪些化合物", "包含", "含有", "有哪些活性成分", "活性成分", "化合物", "成分"],
            )
            if herb_name and not self._looks_like_prescription(herb_name):
                self._graph_tools.find_herb_compounds(herb_name)
                return True

        if ("疾病" in normalized or "病" in normalized) and self._contains_any(normalized, ["靶点", "基因", "target", "Target"]):
            disease_name = self._extract_single_before_any(
                normalized,
                ["关联哪些靶点", "相关靶点", "有哪些靶点", "有什么靶点", "关联哪些基因", "相关基因", "有哪些基因", "有什么基因", "关联", "相关", "靶点", "基因"],
            )
            if disease_name:
                self._graph_tools.find_disease_targets(disease_name)
                return True

        if ("疾病" in normalized or "病" in normalized) and (
            "相关中药" in normalized
            or "哪些中药" in normalized
            or "中药有哪些" in normalized
            or "治疗中药" in normalized
            or ("中药" in normalized and self._contains_any(normalized, ["治疗", "治"]))
        ):
            disease_name = self._extract_single_before_any(normalized, ["有哪些相关中药", "有哪些中药", "相关中药", "治疗中药", "中药"])
            if not disease_name or disease_name.startswith(("治疗", "治")):
                disease_name = self._extract_after_any_before_any(
                    normalized,
                    ["可以治疗", "能治疗", "治疗", "治"],
                    ["的相关中药", "的中药", "相关中药", "中药", "有哪些", "有哪"],
                )
            if disease_name:
                self._graph_tools.find_disease_herbs(disease_name)
                return True

        if ("疾病" in normalized or "病" in normalized) and (
            "相关方剂" in normalized
            or "哪些方剂" in normalized
            or "方剂有哪些" in normalized
            or "治疗方剂" in normalized
            or ("方剂" in normalized and self._contains_any(normalized, ["治疗", "治"]))
        ):
            disease_name = self._extract_single_before_any(normalized, ["有哪些相关方剂", "有哪些方剂", "相关方剂", "治疗方剂", "方剂"])
            if not disease_name or disease_name.startswith(("治疗", "治")):
                disease_name = self._extract_after_any_before_any(
                    normalized,
                    ["可以治疗", "能治疗", "治疗", "治"],
                    ["的相关方剂", "的方剂", "相关方剂", "方剂", "有哪些", "有哪"],
                )
            if disease_name:
                self._graph_tools.find_disease_prescriptions(disease_name)
                return True

        if self._contains_any(normalized, ["组成", "配伍", "有哪些药", "有哪些中药", "含哪些药", "包括哪些药", "由什么组成"]):
            prescription_name = self._extract_single_before_any(normalized, ["由什么组成", "有哪些中药", "有哪些药", "含哪些药", "包括哪些药", "组成", "配伍"])
            if prescription_name and self._looks_like_prescription(prescription_name):
                self._graph_tools.find_prescription_herbs(prescription_name)
                return True

        if self._contains_any(
            normalized,
            ["可以治什么病", "能治什么病", "治什么病", "治疗什么病", "治啥", "主治什么", "主治", "功效", "适应症", "适应证", "作用", "用途", "有什么用", "干什么", "干嘛", "补什么"],
        ):
            subject = self._extract_single_before_any(
                normalized,
                ["可以治什么病", "能治什么病", "治什么病", "治疗什么病", "治啥", "主治什么", "主治", "有什么功效", "有何功效", "功效", "适应症", "适应证", "有什么作用", "有什么用", "有何作用", "作用", "用途", "干什么", "干嘛", "补什么"],
            )
            if subject:
                if self._looks_like_prescription(subject):
                    self._graph_tools.find_prescription_clinical_use(subject)
                else:
                    self._graph_tools.find_herb_clinical_use(subject)
                return True

        if "治疗哪些疾病" in normalized or "关联哪些疾病" in normalized:
            herb_name = self._extract_single_before_any(normalized, ["治疗哪些疾病", "关联哪些疾病", "疾病"])
            if herb_name:
                self._graph_tools.find_herb_diseases(herb_name)
                return True

        if "证候" in normalized and "症状" in normalized:
            syndrome_name = self._extract_single_before_any(normalized, ["包含", "关联", "症状"])
            if syndrome_name:
                self._graph_tools.find_syndrome_symptoms(syndrome_name)
                return True

        if "方剂" in normalized and "症状" in normalized:
            prescription_name = self._extract_single_before_any(normalized, ["治疗", "关联", "症状"])
            if prescription_name:
                self._graph_tools.find_prescription_symptoms(prescription_name)
                return True

        if "方剂" in normalized and "证候" in normalized:
            prescription_name = self._extract_single_before_any(normalized, ["治疗", "关联", "证候"])
            if prescription_name:
                self._graph_tools.find_prescription_syndromes(prescription_name)
                return True

        if "方剂" in normalized or "中药组成" in normalized or "包含哪些中药" in normalized:
            prescription_name = self._extract_single_before_any(normalized, ["包含", "组成", "有哪些中药", "中药"])
            if prescription_name:
                self._graph_tools.find_prescription_herbs(prescription_name)
                return True

        if self._looks_like_broad_tcm_question(normalized):
            self._graph_tools.find_by_semantic_intent(question)
            return True

        return False

    def _looks_like_compound_target_question(self, text: str) -> bool:
        return self._contains_any(text, ["化合物", "成分", "活性成分", "有效成分"]) and self._contains_any(
            text,
            ["靶标", "靶点", "基因", "target", "Target"],
        )

    def _looks_like_broad_tcm_question(self, text: str) -> bool:
        return self._contains_any(
            text,
            ["方剂", "处方", "中药", "药材", "疾病", "症状", "证候", "治疗", "调理", "上火", "口干", "口舌生疮", "咽喉肿痛", "牙龈肿痛", "怕冷", "乏力", "失眠", "头痛", "腹痛", "咳嗽", "发热"],
        )

    def _extract_semantic_topic(self, text: str) -> str | None:
        index = text.find("相关")
        if index <= 0:
            return None
        return self._clean_entity_text(text[:index])

    def _extract_herb_after_semantic_topic(self, text: str) -> str | None:
        index = text.find("相关")
        if index < 0:
            return None
        tail = text[index + len("相关") :]
        if tail.startswith(("的", "于")):
            tail = tail[1:]
        end = self._first_index_of_any(tail, ["活性成分", "有效成分", "化合物", "成分", "靶标", "靶点", "基因"])
        if end > 0:
            tail = tail[:end]
        return self._clean_entity_text(tail)

    def _first_index_of_any(self, text: str, keywords: list[str]) -> int:
        indexes = [text.find(keyword) for keyword in keywords if text.find(keyword) >= 0]
        return min(indexes) if indexes else -1

    def _first_matched(self, text: str, keywords: list[str]) -> str:
        for keyword in keywords:
            if keyword in text:
                return keyword
        return keywords[0] if keywords else ""

    def _contains_any(self, text: str, keywords: list[str]) -> bool:
        return any(keyword in text for keyword in keywords)

    def _looks_like_prescription(self, subject: str) -> bool:
        return self._contains_any(subject, ["方", "丸", "散", "汤", "膏", "丹", "胶囊", "颗粒", "片", "口服液", "饮", "剂"])

    def _extract_entities_before(self, text: str, keyword: str) -> list[str]:
        index = text.find(keyword)
        if index <= 0:
            return []
        prefix = self._after_last_query_word(text[:index])
        prefix = self._clean_entity_text(prefix)
        entities = [self._clean_entity_text(part) for part in re.split(r"以及|和|与|及|跟|、|,|，|;|；|/", prefix)]
        entities = [entity for entity in entities if entity]
        return entities if len(entities) >= 2 else []

    def _extract_single_before_any(self, text: str, keywords: list[str]) -> str | None:
        indexes = [text.find(keyword) for keyword in keywords if text.find(keyword) > 0]
        if not indexes:
            return None
        return self._clean_entity_text(self._after_last_query_word(text[: min(indexes)]))

    def _extract_after_any_before_any(self, text: str, markers: list[str], stop_words: list[str]) -> str | None:
        start = -1
        marker_length = 0
        for marker in markers:
            candidate = text.rfind(marker)
            if candidate >= 0 and (candidate > start or (candidate == start and len(marker) > marker_length)):
                start = candidate
                marker_length = len(marker)
        if start < 0:
            return None
        tail = text[start + marker_length :]
        end = len(tail)
        for stop_word in stop_words:
            candidate = tail.find(stop_word)
            if 0 <= candidate < end:
                end = candidate
        return self._clean_entity_text(tail[:end])

    def _after_last_query_word(self, text: str) -> str:
        result = text
        for word in self.QUERY_WORDS:
            index = result.rfind(word)
            if index >= 0:
                result = result[index + len(word) :]
        return result

    def _clean_entity_text(self, text: str | None) -> str:
        if text is None:
            return ""
        cleaned = re.sub(r"请调用.*?工具", "", text)
        cleaned = re.sub(r"只根据.*", "", cleaned)
        cleaned = re.sub(r"^(方剂|中药|疾病|证候|化合物|通路|医案)", "", cleaned)
        for token in ["有哪些", "有什么", "有何", "的", "哪些", "什么", "啥", "可以", "能够", "能", "有关", "相关", "一下"]:
            cleaned = cleaned.replace(token, "")
        cleaned = re.sub(r"^[：:，,。\s]+", "", cleaned)
        cleaned = re.sub(r"[：:，,。？?\s]+$", "", cleaned)
        return cleaned.strip()
