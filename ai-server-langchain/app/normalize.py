from __future__ import annotations

import re

from app.repository import TcmGraphRepository


DISEASE_ALIASES: list[tuple[str, str]] = [
    ("2型糖尿病", "type 2 diabetes mellitus"),
    ("二型糖尿病", "type 2 diabetes mellitus"),
    ("ii型糖尿病", "type 2 diabetes mellitus"),
    ("type2diabetes", "type 2 diabetes mellitus"),
    ("1型糖尿病", "type 1 diabetes mellitus"),
    ("一型糖尿病", "type 1 diabetes mellitus"),
    ("i型糖尿病", "type 1 diabetes mellitus"),
    ("type1diabetes", "type 1 diabetes mellitus"),
    ("糖尿病", "diabetes mellitus"),
    ("高血压", "hypertension"),
    ("冠心病", "coronary artery disease"),
    ("新型冠状病毒感染", "Covid-19"),
    ("新冠肺炎", "Covid-19"),
    ("新冠", "Covid-19"),
    ("肺癌", "lung cancer"),
    ("乳腺癌", "breast cancer"),
    ("结直肠癌", "colorectal cancer"),
    ("胃癌", "stomach cancer"),
    ("肝癌", "liver cancer"),
    ("哮喘", "asthma"),
    ("肥胖", "obesity"),
    ("抑郁症", "depressive disorder"),
]


class EntityNormalizeService:
    def __init__(self, graph_repository: TcmGraphRepository) -> None:
        self._graph_repository = graph_repository

    def normalize_disease(self, value: str | None) -> str:
        cleaned = self._clean(value)
        if not cleaned:
            return ""
        compacted = self._compact(cleaned)
        for alias, normalized in DISEASE_ALIASES:
            if alias.lower().replace(" ", "") in compacted:
                return normalized
        return self._first_value_or_default(
            """
            WITH toLower($name) AS q
            MATCH (d:Disease)
            WHERE toLower(d.disease_name) = q
               OR toLower(d.disease_name) CONTAINS q
               OR q CONTAINS toLower(d.disease_name)
            RETURN DISTINCT d.disease_name AS value,
            CASE
                WHEN toLower(d.disease_name) = q THEN 0
                WHEN toLower(d.disease_name) STARTS WITH q THEN 1
                WHEN q CONTAINS toLower(d.disease_name) THEN 2
                ELSE 3
            END AS score
            ORDER BY score, size(d.disease_name)
            LIMIT 1
            """,
            {"name": cleaned},
            cleaned,
        )

    def normalize_herb(self, value: str | None) -> str:
        cleaned = self._clean(value)
        if not cleaned:
            return ""
        return self._first_value_or_default(
            """
            MATCH (h)
            WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
              AND (h.herb_name_zh = $name
                   OR h.herb_name_zh CONTAINS $name
                   OR $name CONTAINS h.herb_name_zh
                   OR toLower(coalesce(h.english_name, '')) = toLower($name)
                   OR toLower(coalesce(h.pinyin_name, '')) = toLower($name))
            RETURN DISTINCT h.herb_name_zh AS value,
            CASE
                WHEN h.herb_name_zh = $name THEN 0
                WHEN $name CONTAINS h.herb_name_zh THEN 1
                WHEN h.herb_name_zh CONTAINS $name THEN 2
                ELSE 3
            END AS score
            ORDER BY score, size(h.herb_name_zh) DESC
            LIMIT 1
            """,
            {"name": cleaned},
            cleaned,
        )

    def normalize_prescription(self, value: str | None) -> str:
        cleaned = self._clean(value)
        if not cleaned:
            return ""
        return self._first_value_or_default(
            """
            MATCH (p:Prescription)
            WHERE p.name_zh = $name
               OR p.name_zh CONTAINS $name
               OR $name CONTAINS p.name_zh
            RETURN DISTINCT p.name_zh AS value,
            CASE
                WHEN p.name_zh = $name THEN 0
                WHEN $name CONTAINS p.name_zh THEN 1
                WHEN p.name_zh CONTAINS $name THEN 2
                ELSE 3
            END AS score
            ORDER BY score, size(p.name_zh) DESC
            LIMIT 1
            """,
            {"name": cleaned},
            cleaned,
        )

    def normalize_syndrome(self, value: str | None) -> str:
        cleaned = self._clean(value)
        if not cleaned:
            return ""
        return self._first_value_or_default(
            """
            MATCH (s:Syndrome)
            WHERE s.syndrome_name_zh = $name
               OR s.syndrome_name_zh CONTAINS $name
               OR $name CONTAINS s.syndrome_name_zh
            RETURN DISTINCT s.syndrome_name_zh AS value,
            CASE WHEN s.syndrome_name_zh = $name THEN 0 ELSE 1 END AS score
            ORDER BY score, size(s.syndrome_name_zh) DESC
            LIMIT 1
            """,
            {"name": cleaned},
            cleaned,
        )

    def normalize_pathway(self, value: str | None) -> str:
        cleaned = self._clean(value)
        if not cleaned:
            return ""
        return self._first_value_or_default(
            """
            MATCH (p:Pathway)
            WHERE p.name = $name
               OR p.pathway_id = $name
               OR toLower(p.name) CONTAINS toLower($name)
            RETURN DISTINCT coalesce(p.name, p.pathway_id) AS value,
            CASE WHEN p.name = $name OR p.pathway_id = $name THEN 0 ELSE 1 END AS score
            ORDER BY score, size(value)
            LIMIT 1
            """,
            {"name": cleaned},
            cleaned,
        )

    def _first_value_or_default(self, cypher: str, params: dict[str, str], default: str) -> str:
        try:
            rows = self._graph_repository.query(cypher, params)
        except Exception:
            return default
        if rows:
            value = rows[0].get("value")
            if value is not None and str(value).strip():
                return str(value)
        return default

    def _clean(self, value: str | None) -> str:
        if value is None:
            return ""
        cleaned = re.sub(r"[“”\"'`]", "", value)
        cleaned = re.sub(r"^(中药|方剂|疾病|证候|症状|通路|靶点|基因)", "", cleaned)
        cleaned = re.sub(r"(相关|关联|治疗|主治|功效|作用|用途|适应症|适应证)$", "", cleaned)
        cleaned = re.sub(r"^[：:，,。\s]+", "", cleaned)
        cleaned = re.sub(r"[：:，,。？?\s]+$", "", cleaned)
        return cleaned.strip()

    def _compact(self, value: str) -> str:
        return re.sub(r"\s+", "", value.lower()).replace("Ⅱ", "ii").replace("Ⅰ", "i")
