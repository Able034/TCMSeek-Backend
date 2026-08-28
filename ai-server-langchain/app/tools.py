from __future__ import annotations

import re
from typing import Any

from pydantic import BaseModel, Field

from app.config import Settings
from app.models import GraphToolResult, SemanticAnchor
from app.normalize import EntityNormalizeService
from app.recorder import ToolExecutionRecorder
from app.repository import TcmGraphRepository
from app.semantic import SemanticSearchService


class HerbNameInput(BaseModel):
    herbName: str = Field(description="中药中文名，例如 人参、黄芪")


class HerbNamesInput(BaseModel):
    herbNames: str = Field(description="两味或多味中药中文名，使用逗号、顿号或“和”分隔")


class PrescriptionNameInput(BaseModel):
    prescriptionName: str = Field(description="方剂中文名，例如 六味地黄丸")


class DiseaseNameInput(BaseModel):
    diseaseName: str = Field(description="疾病名称，例如 糖尿病 或 diabetes mellitus")


class SyndromeNameInput(BaseModel):
    syndromeName: str = Field(description="证候中文名")


class CompoundInput(BaseModel):
    compound: str = Field(description="化合物标识，可以是 InChIKey、中文名或英文名")


class PathwayNameInput(BaseModel):
    pathwayName: str = Field(description="通路名称或通路ID")


class CaseIdInput(BaseModel):
    caseId: str = Field(description="医案ID")


class SemanticQuestionInput(BaseModel):
    question: str = Field(description="用户完整原始问题")


class HerbTopicInput(BaseModel):
    herbName: str = Field(description="中药中文名，例如 人参 或 黄芪")
    topic: str = Field(description="用户问题中的生物学主题短语，例如 抗炎、巨噬细胞炎症、降糖")


class TcmGraphTools:
    ANTI_INFLAMMATION_TARGETS = [
        "TNF",
        "IL6",
        "IL1B",
        "PTGS2",
        "NFKB1",
        "RELA",
        "CXCL8",
        "TLR4",
        "MAPK1",
        "MAPK3",
        "JUN",
        "STAT3",
        "NOS2",
        "NLRP3",
        "CCL2",
    ]
    MACROPHAGE_INFLAMMATION_TARGETS = [
        "TNF",
        "IL6",
        "IL1B",
        "TLR4",
        "NFKB1",
        "RELA",
        "MAPK1",
        "MAPK3",
        "JUN",
        "STAT3",
        "PTGS2",
        "NOS2",
        "NLRP3",
        "CCL2",
        "CXCL8",
        "IL10",
    ]
    TOPIC_TARGET_SEMANTIC_TYPES = ["topic", "target"]
    GENERAL_SEMANTIC_TYPES = [
        "prescription",
        "syndrome",
        "tcm_symptom",
        "wm_symptom",
        "disease",
        "herb",
        "topic",
        "target",
    ]
    DISEASE_SEMANTIC_TYPES = ["disease"]
    DISEASE_PRESCRIPTION_FALLBACK_TYPES = ["prescription", "syndrome", "wm_symptom"]
    CONDITION_PRESCRIPTION_SEMANTIC_TYPES = ["disease", "wm_symptom", "tcm_symptom", "syndrome"]
    TOPIC_MAX_DISTANCE = 0.45
    TARGET_MAX_DISTANCE = 0.38
    GENERAL_SEMANTIC_MAX_DISTANCE = {
        "prescription": 0.42,
        "syndrome": 0.42,
        "tcm_symptom": 0.40,
        "wm_symptom": 0.42,
        "disease": 0.36,
        "herb": 0.36,
        "topic": 0.45,
        "target": 0.38,
    }

    def __init__(
        self,
        graph_repository: TcmGraphRepository,
        normalizer: EntityNormalizeService,
        recorder: ToolExecutionRecorder,
        settings: Settings,
        semantic_search_service: SemanticSearchService | None = None,
    ) -> None:
        self._graph_repository = graph_repository
        self._normalizer = normalizer
        self._recorder = recorder
        self._settings = settings
        self._semantic_search_service = semantic_search_service

    def as_langchain_tools(self) -> list[Any]:
        from langchain_core.tools import StructuredTool

        return [
            StructuredTool.from_function(
                name="findHerbCompounds",
                description="查询某味中药包含的化合物。参数必须是中药中文名，例如 人参、黄芪。",
                func=lambda herbName: self.find_herb_compounds(herbName).model_dump(),
                args_schema=HerbNameInput,
            ),
            StructuredTool.from_function(
                name="findHerbCompoundTargets",
                description="一次性查询某味中药包含的化合物，以及这些化合物直接作用的靶点或靶标。",
                func=lambda herbName: self.find_herb_compound_targets(herbName).model_dump(),
                args_schema=HerbNameInput,
            ),
            StructuredTool.from_function(
                name="findCommonTargets",
                description="查询两味或多味中药通过化合物共同作用的靶点。",
                func=lambda herbNames: self.find_common_targets(herbNames).model_dump(),
                args_schema=HerbNamesInput,
            ),
            StructuredTool.from_function(
                name="findCommonCompounds",
                description="查询两味或多味中药共同包含的化合物或共同活性成分。",
                func=lambda herbNames: self.find_common_compounds(herbNames).model_dump(),
                args_schema=HerbNamesInput,
            ),
            StructuredTool.from_function(
                name="findHerbClinicalUse",
                description="综合查询某味中药的功效主治、关联症状、关联证候、直接治疗疾病。",
                func=lambda herbName: self.find_herb_clinical_use(herbName).model_dump(),
                args_schema=HerbNameInput,
            ),
            StructuredTool.from_function(
                name="findHerbDiseases",
                description="查询某味中药治疗或关联的疾病。",
                func=lambda herbName: self.find_herb_diseases(herbName).model_dump(),
                args_schema=HerbNameInput,
            ),
            StructuredTool.from_function(
                name="findPrescriptionHerbs",
                description="查询某个方剂包含的中药。",
                func=lambda prescriptionName: self.find_prescription_herbs(prescriptionName).model_dump(),
                args_schema=PrescriptionNameInput,
            ),
            StructuredTool.from_function(
                name="findPrescriptionClinicalUse",
                description="综合查询某个方剂的组成中药、功效主治、关联疾病、关联证候、关联症状。",
                func=lambda prescriptionName: self.find_prescription_clinical_use(prescriptionName).model_dump(),
                args_schema=PrescriptionNameInput,
            ),
            StructuredTool.from_function(
                name="findDiseaseTargets",
                description="查询某个疾病关联的靶点。疾病参数可以是中文名或图谱英文名。",
                func=lambda diseaseName: self.find_disease_targets(diseaseName).model_dump(),
                args_schema=DiseaseNameInput,
            ),
            StructuredTool.from_function(
                name="findDiseaseHerbs",
                description="查询某个疾病关联的中药。",
                func=lambda diseaseName: self.find_disease_herbs(diseaseName).model_dump(),
                args_schema=DiseaseNameInput,
            ),
            StructuredTool.from_function(
                name="findDiseasePrescriptions",
                description="查询某个疾病关联的方剂。",
                func=lambda diseaseName: self.find_disease_prescriptions(diseaseName).model_dump(),
                args_schema=DiseaseNameInput,
            ),
            StructuredTool.from_function(
                name="findPrescriptionSymptoms",
                description="查询某个方剂治疗或关联的症状。",
                func=lambda prescriptionName: self.find_prescription_symptoms(prescriptionName).model_dump(),
                args_schema=PrescriptionNameInput,
            ),
            StructuredTool.from_function(
                name="findPrescriptionSyndromes",
                description="查询某个方剂治疗或关联的证候。",
                func=lambda prescriptionName: self.find_prescription_syndromes(prescriptionName).model_dump(),
                args_schema=PrescriptionNameInput,
            ),
            StructuredTool.from_function(
                name="findSyndromeSymptoms",
                description="查询某个证候包含或关联的症状。",
                func=lambda syndromeName: self.find_syndrome_symptoms(syndromeName).model_dump(),
                args_schema=SyndromeNameInput,
            ),
            StructuredTool.from_function(
                name="findCompoundTargets",
                description="查询某个化合物作用的靶点。",
                func=lambda compound: self.find_compound_targets(compound).model_dump(),
                args_schema=CompoundInput,
            ),
            StructuredTool.from_function(
                name="findPathwayTargets",
                description="查询某个通路包含的靶点或基因。",
                func=lambda pathwayName: self.find_pathway_targets(pathwayName).model_dump(),
                args_schema=PathwayNameInput,
            ),
            StructuredTool.from_function(
                name="findMedicalCasePrescriptions",
                description="查询某个医案使用的方剂。",
                func=lambda caseId: self.find_medical_case_prescriptions(caseId).model_dump(),
                args_schema=CaseIdInput,
            ),
            StructuredTool.from_function(
                name="findBySemanticIntent",
                description="通用语义路由工具：当实体名不精确、问题较口语化或症状/证候较模糊时使用。",
                func=lambda question: self.find_by_semantic_intent(question).model_dump(),
                args_schema=SemanticQuestionInput,
            ),
            StructuredTool.from_function(
                name="findHerbCompoundTargetsByTopic",
                description="按生物学主题筛选某味中药化合物及靶点，例如 抗炎相关的人参活性成分和靶点。",
                func=lambda herbName, topic: self.find_herb_compound_targets_by_topic(herbName, topic).model_dump(),
                args_schema=HerbTopicInput,
            ),
        ]

    def find_herb_compounds(self, herb_name: str) -> GraphToolResult:
        normalized = self._normalizer.normalize_herb(herb_name)
        cypher = """
        MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
          AND h.herb_name_zh = $herbName
        RETURN DISTINCT
          coalesce(c.name_zh, c.name, c.inchikey) AS compound,
          c.inchikey AS inchikey,
          c.molecular_formula AS formula
        LIMIT $limit
        """
        params = self._base_params("herbName", normalized)
        params["originalHerbName"] = herb_name
        return self._query("findHerbCompounds", "herb_compounds", cypher, params)

    def find_herb_compound_targets(self, herb_name: str) -> GraphToolResult:
        normalized = self._normalizer.normalize_herb(herb_name)
        cypher = """
        MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound)-[:TARGETS]->(t:Target)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
          AND h.herb_name_zh = $herbName
        RETURN DISTINCT
          h.herb_name_zh AS herb,
          coalesce(c.name_zh, c.name, c.inchikey) AS compound,
          c.inchikey AS inchikey,
          c.molecular_formula AS formula,
          t.symbol AS target,
          t.tcm_tar_id AS targetId
        ORDER BY compound, target
        LIMIT $limit
        """
        params = self._base_params("herbName", normalized)
        params["originalHerbName"] = herb_name
        return self._query("findHerbCompoundTargets", "herb_compound_targets", cypher, params)

    def find_common_compounds(self, herb_names: str) -> GraphToolResult:
        herbs = self._normalize_herb_names(herb_names)
        params = {"herbNames": herbs, "originalHerbNames": herb_names, "limit": self._tool_limit()}
        if len(herbs) < 2:
            return self._record("findCommonCompounds", params, "common_compounds", [])
        cypher = """
        MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
          AND h.herb_name_zh IN $herbNames
        WITH c, collect(DISTINCT h.herb_name_zh) AS matchedHerbs
        WHERE size(matchedHerbs) = size($herbNames)
        RETURN DISTINCT
          coalesce(c.name_zh, c.name, c.inchikey) AS compound,
          c.inchikey AS inchikey,
          c.molecular_formula AS formula,
          matchedHerbs AS herbs
        ORDER BY compound
        LIMIT $limit
        """
        return self._query("findCommonCompounds", "common_compounds", cypher, params)

    def find_common_targets(self, herb_names: str) -> GraphToolResult:
        herbs = self._normalize_herb_names(herb_names)
        params = {"herbNames": herbs, "originalHerbNames": herb_names, "limit": self._tool_limit()}
        if len(herbs) < 2:
            return self._record("findCommonTargets", params, "common_targets", [])
        cypher = """
        MATCH (h)-[:CONTAINS_COMPOUND]->(:Compound)-[:TARGETS]->(t:Target)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
          AND h.herb_name_zh IN $herbNames
        WITH t, collect(DISTINCT h.herb_name_zh) AS matchedHerbs
        WHERE size(matchedHerbs) = size($herbNames)
        RETURN DISTINCT
          t.symbol AS target,
          t.tcm_tar_id AS targetId,
          matchedHerbs AS herbs
        ORDER BY target
        LIMIT $limit
        """
        return self._query("findCommonTargets", "common_targets", cypher, params)

    def find_herb_clinical_use(self, herb_name: str) -> GraphToolResult:
        params = self._base_params("herbName", self._normalizer.normalize_herb(herb_name))
        params["originalHerbName"] = herb_name
        rows: list[dict[str, Any]] = []
        rows.extend(self._graph_repository.query(
            """
            MATCH (h)
            WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
              AND h.herb_name_zh = $herbName
            RETURN DISTINCT
              'herb_profile' AS category,
              h.herb_name_zh AS herb,
              coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId,
              h.efficacy_zh AS efficacy,
              h.indications_zh AS indications,
              h.nature_taste_zh AS natureTaste,
              h.meridian_zh AS meridians,
              h.latin_name AS latinName,
              h.english_name AS englishName
            LIMIT 5
            """,
            params,
        ))
        rows.extend(self._graph_repository.query(
            """
            MATCH (h)-[:TREATS_SYMPTOM]->(s)
            WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
              AND h.herb_name_zh = $herbName
            RETURN DISTINCT 'symptom' AS category,
              coalesce(s.symptom_name_zh, s.symptom_name) AS name,
              s.tcm_symptom_id AS id
            ORDER BY name
            LIMIT 30
            """,
            params,
        ))
        rows.extend(self._graph_repository.query(
            """
            MATCH (h)-[:TREATS_SYNDROME]->(s:Syndrome)
            WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
              AND h.herb_name_zh = $herbName
            RETURN DISTINCT 'syndrome' AS category,
              s.syndrome_name_zh AS name,
              s.tcm_syndrome_id AS id
            ORDER BY name
            LIMIT 30
            """,
            params,
        ))
        direct_diseases = self._graph_repository.query(
            """
            MATCH (h)-[:TREATS_DISEASE]->(d:Disease)
            WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
              AND h.herb_name_zh = $herbName
            RETURN DISTINCT 'direct_disease' AS category,
              d.disease_name AS disease,
              d.disease_id AS diseaseId,
              'direct_herb_disease' AS evidenceType
            ORDER BY disease
            LIMIT 30
            """,
            params,
        )
        rows.extend(direct_diseases)
        if not direct_diseases:
            rows.extend(self._graph_repository.query(
                """
                MATCH (h)<-[:CONTAINS_HERB]-(p:Prescription)-[:TREATS_DISEASE]->(d:Disease)
                WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
                  AND h.herb_name_zh = $herbName
                WITH d, count(DISTINCT p) AS evidenceCount, collect(DISTINCT p.name_zh)[0..5] AS evidencePrescriptions
                RETURN DISTINCT 'indirect_prescription_disease' AS category,
                  d.disease_name AS disease,
                  d.disease_id AS diseaseId,
                  evidenceCount AS evidenceCount,
                  evidencePrescriptions AS evidencePrescriptions,
                  'prescription_contains_herb' AS evidenceType
                ORDER BY evidenceCount DESC, disease
                LIMIT 30
                """,
                params,
            ))
        return self._record("findHerbClinicalUse", params, "herb_clinical_use", rows)

    def find_herb_diseases(self, herb_name: str) -> GraphToolResult:
        params = self._base_params("herbName", self._normalizer.normalize_herb(herb_name))
        params["originalHerbName"] = herb_name
        rows = self._graph_repository.query(
            """
            MATCH (h)-[:TREATS_DISEASE]->(d:Disease)
            WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
              AND h.herb_name_zh = $herbName
            RETURN DISTINCT d.disease_name AS disease,
              d.disease_id AS diseaseId,
              'direct_herb_disease' AS evidenceType
            LIMIT $limit
            """,
            params,
        )
        if not rows:
            rows = self._graph_repository.query(
                """
                MATCH (p:Prescription)-[:CONTAINS_HERB]->(h)
                WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
                  AND h.herb_name_zh = $herbName
                WITH DISTINCT p, h
                MATCH (p)-[:TREATS_DISEASE]->(d:Disease)
                WITH d, collect(DISTINCT p.name_zh)[0..5] AS prescriptions
                RETURN DISTINCT d.disease_name AS disease,
                  d.disease_id AS diseaseId,
                  prescriptions AS evidencePrescriptions,
                  'prescription_contains_herb' AS evidenceType
                ORDER BY disease
                LIMIT $limit
                """,
                params,
            )
        return self._record("findHerbDiseases", params, "herb_diseases", rows)

    def find_prescription_herbs(self, prescription_name: str) -> GraphToolResult:
        params = self._base_params("prescriptionName", self._normalizer.normalize_prescription(prescription_name))
        params["originalPrescriptionName"] = prescription_name
        cypher = """
        MATCH (p:Prescription {name_zh: $prescriptionName})-[:CONTAINS_HERB]->(h)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
        RETURN DISTINCT h.herb_name_zh AS herb
        LIMIT $limit
        """
        return self._query("findPrescriptionHerbs", "prescription_herbs", cypher, params)

    def find_prescription_clinical_use(self, prescription_name: str) -> GraphToolResult:
        params = self._base_params("prescriptionName", self._normalizer.normalize_prescription(prescription_name))
        params["originalPrescriptionName"] = prescription_name
        queries = [
            """
            MATCH (p:Prescription {name_zh: $prescriptionName})
            RETURN DISTINCT 'prescription_profile' AS category,
              p.name_zh AS prescription,
              p.tcm_prescription_id AS prescriptionId,
              p.effects_zh AS effects,
              p.indications_zh AS indications,
              p.source AS source
            LIMIT 5
            """,
            """
            MATCH (p:Prescription {name_zh: $prescriptionName})-[:CONTAINS_HERB]->(h)
            WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
            RETURN DISTINCT 'herb' AS category,
              h.herb_name_zh AS herb,
              coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId
            ORDER BY herb
            LIMIT 50
            """,
            """
            MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_DISEASE]->(d:Disease)
            RETURN DISTINCT 'disease' AS category,
              d.disease_name AS disease,
              d.disease_id AS diseaseId
            ORDER BY disease
            LIMIT 30
            """,
            """
            MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYNDROME]->(s:Syndrome)
            RETURN DISTINCT 'syndrome' AS category,
              s.syndrome_name_zh AS name,
              s.tcm_syndrome_id AS id
            ORDER BY name
            LIMIT 30
            """,
            """
            MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYMPTOM]->(s)
            RETURN DISTINCT 'symptom' AS category,
              coalesce(s.symptom_name_zh, s.symptom_name) AS name,
              s.tcm_symptom_id AS id
            ORDER BY name
            LIMIT 30
            """,
        ]
        rows: list[dict[str, Any]] = []
        for query in queries:
            rows.extend(self._graph_repository.query(query, params))
        return self._record("findPrescriptionClinicalUse", params, "prescription_clinical_use", rows)

    def find_prescription_symptoms(self, prescription_name: str) -> GraphToolResult:
        params = self._base_params("prescriptionName", self._normalizer.normalize_prescription(prescription_name))
        params["originalPrescriptionName"] = prescription_name
        cypher = """
        MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYMPTOM]->(s:Symptom)
        RETURN DISTINCT coalesce(s.symptom_name_zh, s.symptom_name) AS symptom
        LIMIT $limit
        """
        return self._query("findPrescriptionSymptoms", "prescription_symptoms", cypher, params)

    def find_prescription_syndromes(self, prescription_name: str) -> GraphToolResult:
        params = self._base_params("prescriptionName", self._normalizer.normalize_prescription(prescription_name))
        params["originalPrescriptionName"] = prescription_name
        cypher = """
        MATCH (p:Prescription {name_zh: $prescriptionName})-[:TREATS_SYNDROME]->(s:Syndrome)
        RETURN DISTINCT s.syndrome_name_zh AS syndrome
        LIMIT $limit
        """
        return self._query("findPrescriptionSyndromes", "prescription_syndromes", cypher, params)

    def find_disease_targets(self, disease_name: str) -> GraphToolResult:
        params = self._base_params("diseaseName", disease_name)
        params["diseaseQuery"] = self._normalizer.normalize_disease(disease_name)
        if not str(params["diseaseQuery"]).strip():
            return self._record("findDiseaseTargets", params, "disease_targets", [])
        cypher = """
        WITH toLower($diseaseQuery) AS q
        MATCH (d:Disease)-[:ASSOCIATED_WITH]-(t:Target)
        WHERE toLower(d.disease_name) = q OR toLower(d.disease_name) CONTAINS q
        WITH DISTINCT d, t, q,
        CASE
            WHEN toLower(d.disease_name) = q THEN 0
            WHEN toLower(d.disease_name) STARTS WITH q THEN 1
            ELSE 2
        END AS matchScore
        RETURN DISTINCT d.disease_name AS disease,
          d.disease_id AS diseaseId,
          t.symbol AS target,
          t.tcm_tar_id AS targetId,
          matchScore AS matchScore
        ORDER BY matchScore, disease, target
        LIMIT $limit
        """
        return self._query("findDiseaseTargets", "disease_targets", cypher, params)

    def find_disease_herbs(self, disease_name: str) -> GraphToolResult:
        params = self._base_params("diseaseName", disease_name)
        params["diseaseQuery"] = self._normalizer.normalize_disease(disease_name)
        if not str(params["diseaseQuery"]).strip():
            return self._record("findDiseaseHerbs", params, "disease_herbs", [])
        cypher = """
        WITH toLower($diseaseQuery) AS q
        MATCH (h)-[:TREATS_DISEASE]->(d:Disease)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
          AND (toLower(d.disease_name) = q OR toLower(d.disease_name) CONTAINS q)
        WITH DISTINCT h, d, q,
        CASE
            WHEN toLower(d.disease_name) = q THEN 0
            WHEN toLower(d.disease_name) STARTS WITH q THEN 1
            ELSE 2
        END AS matchScore
        RETURN DISTINCT d.disease_name AS disease,
          d.disease_id AS diseaseId,
          h.herb_name_zh AS herb,
          coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId,
          matchScore AS matchScore
        ORDER BY matchScore, disease, herb
        LIMIT $limit
        """
        return self._query("findDiseaseHerbs", "disease_herbs", cypher, params)

    def find_disease_prescriptions(self, disease_name: str) -> GraphToolResult:
        params = self._base_params("diseaseName", disease_name)
        params["diseaseQuery"] = self._normalizer.normalize_disease(disease_name)
        if not str(params["diseaseQuery"]).strip():
            return self._record("findDiseasePrescriptions", params, "disease_prescriptions", [])
        cypher = """
        WITH toLower($diseaseQuery) AS q
        MATCH (p:Prescription)-[:TREATS_DISEASE]->(d:Disease)
        WHERE toLower(d.disease_name) = q OR toLower(d.disease_name) CONTAINS q
        WITH DISTINCT p, d, q,
        CASE
            WHEN toLower(d.disease_name) = q THEN 0
            WHEN toLower(d.disease_name) STARTS WITH q THEN 1
            ELSE 2
        END AS matchScore
        RETURN DISTINCT d.disease_name AS disease,
          d.disease_id AS diseaseId,
          p.name_zh AS prescription,
          p.tcm_prescription_id AS prescriptionId,
          matchScore AS matchScore
        ORDER BY matchScore, disease, prescription
        LIMIT $limit
        """
        rows = self._graph_repository.query(cypher, params)
        if rows:
            return self._record("findDiseasePrescriptions", params, "disease_prescriptions", rows)
        semantic = self._find_disease_prescriptions_by_semantic_disease(disease_name, params)
        if semantic.items:
            self._recorder.record("findDiseasePrescriptions", params, semantic)
            return semantic
        fallback = self._find_disease_prescriptions_by_semantic_fallback(disease_name, params)
        self._recorder.record("findDiseasePrescriptions", params, fallback)
        return fallback

    def find_syndrome_symptoms(self, syndrome_name: str) -> GraphToolResult:
        params = self._base_params("syndromeName", self._normalizer.normalize_syndrome(syndrome_name))
        params["originalSyndromeName"] = syndrome_name
        cypher = """
        MATCH (s:Syndrome {syndrome_name_zh: $syndromeName})-[:HAS_SYMPTOM]->(sym:Symptom)
        RETURN DISTINCT coalesce(sym.symptom_name_zh, sym.symptom_name) AS symptom
        LIMIT $limit
        """
        return self._query("findSyndromeSymptoms", "syndrome_symptoms", cypher, params)

    def find_compound_targets(self, compound: str) -> GraphToolResult:
        params = self._base_params("compound", compound)
        cypher = """
        MATCH (c:Compound)-[:TARGETS]->(t:Target)
        WHERE c.inchikey = $compound OR c.name = $compound OR c.name_zh = $compound
        RETURN DISTINCT t.symbol AS target
        LIMIT $limit
        """
        return self._query("findCompoundTargets", "compound_targets", cypher, params)

    def find_pathway_targets(self, pathway_name: str) -> GraphToolResult:
        params = self._base_params("pathwayName", self._normalizer.normalize_pathway(pathway_name))
        params["originalPathwayName"] = pathway_name
        cypher = """
        MATCH (p:Pathway)-[:CONTAINS_GENE]->(t:Target)
        WHERE p.name = $pathwayName OR p.pathway_id = $pathwayName
        RETURN DISTINCT t.symbol AS target
        LIMIT $limit
        """
        return self._query("findPathwayTargets", "pathway_targets", cypher, params)

    def find_medical_case_prescriptions(self, case_id: str) -> GraphToolResult:
        params = self._base_params("caseId", case_id)
        cypher = """
        MATCH (m:MedicalCase {med_case_id: $caseId})-[:USES_PRESCRIPTION]->(p:Prescription)
        RETURN DISTINCT p.name_zh AS prescription
        LIMIT $limit
        """
        return self._query("findMedicalCasePrescriptions", "medicalcase_prescriptions", cypher, params)

    def find_by_semantic_intent(self, question: str) -> GraphToolResult:
        params = {"question": question, "limit": self._tool_limit(), "router": "python-lightweight"}
        if self._semantic_ready():
            params["semanticQueryTypes"] = self.GENERAL_SEMANTIC_TYPES
            params["semanticMaxDistanceByType"] = self.GENERAL_SEMANTIC_MAX_DISTANCE
            if self._asks_prescription_for_condition(question):
                condition = self._find_prescriptions_by_semantic_condition(question, params)
                if condition.items:
                    self._recorder.record("findBySemanticIntent", params, condition)
                    return condition
            candidates = self._semantic_search_service.search(  # type: ignore[union-attr]
                question,
                self.GENERAL_SEMANTIC_TYPES,
                min(40, max(12, self._tool_limit())),
            )
            anchors = self._accepted_general_semantic_anchors(candidates)
            params["semanticCandidateAnchors"] = self._anchor_summaries(candidates, 12)
            params["semanticAnchors"] = self._anchor_summaries(anchors, 12)
            params["semanticCandidateCount"] = len(candidates)
            params["semanticAnchorCount"] = len(anchors)
            rows: list[dict[str, Any]] = []
            routed_keys: set[str] = set()
            for anchor in anchors[:8]:
                key = f"{anchor.entityType}:{anchor.name}"
                if key in routed_keys:
                    continue
                routed_keys.add(key)
                rows.extend(self._route_semantic_anchor(question, anchor, max(3, min(12, self._tool_limit()))))
            params["routedAnchorCount"] = len(routed_keys)
            return self._record("findBySemanticIntent", params, "semantic_intent", rows)

        normalized = re.sub(r"\s+", "", question or "")
        if "方剂" in normalized or "方子" in normalized or "药方" in normalized:
            disease = self._extract_after_any_before_any(
                normalized,
                ["治疗", "治", "调理", "缓解"],
                ["的方剂", "方剂", "方子", "药方", "有哪些", "推荐"],
            )
            disease = disease or self._clean_entity_text(normalized)
            routed = self.find_disease_prescriptions(disease)
            return self._record(
                "findBySemanticIntent",
                params | {"routedTool": "findDiseasePrescriptions", "routedDiseaseName": disease},
                routed.queryType,
                routed.items,
            )
        return self._record("findBySemanticIntent", params, "semantic_intent", [])

    def find_herb_compound_targets_by_topic(self, herb_name: str, topic: str) -> GraphToolResult:
        normalized = self._normalizer.normalize_herb(herb_name)
        symbols = self._curated_topic_target_symbols(topic)
        params = {
            "herbName": normalized,
            "originalHerbName": herb_name,
            "topic": topic,
            "limit": self._tool_limit(),
            "semanticQueryTypes": self.TOPIC_TARGET_SEMANTIC_TYPES,
        }
        if self._semantic_ready():
            anchors = self._semantic_search_service.search(  # type: ignore[union-attr]
                topic,
                self.TOPIC_TARGET_SEMANTIC_TYPES,
                min(30, max(10, self._tool_limit())),
            )
            accepted = self._accepted_topic_semantic_anchors(anchors)
            for symbol in self._semantic_search_service.exact_topic_target_symbols(topic):  # type: ignore[union-attr]
                if symbol not in symbols:
                    symbols.append(symbol)
            for symbol in self._semantic_search_service.target_symbols_from_anchors(accepted):  # type: ignore[union-attr]
                if symbol not in symbols:
                    symbols.append(symbol)
            params["semanticCandidateAnchors"] = self._anchor_summaries(anchors, 10)
            params["semanticAnchors"] = self._anchor_summaries(accepted, 10)
            params["semanticCandidateCount"] = len(anchors)
            params["semanticAnchorCount"] = len(accepted)
        else:
            params["semanticProvider"] = "curated-python-seed"
        symbols = symbols[: max(20, min(200, self._tool_limit()))]
        params["targetSymbols"] = symbols
        params["targetSymbolsUpper"] = [item.upper() for item in symbols]
        if not normalized or not topic or not symbols:
            return self._record("findHerbCompoundTargetsByTopic", params, "herb_compound_targets_semantic", [])
        cypher = """
        MATCH (h)-[:CONTAINS_COMPOUND]->(c:Compound)-[:TARGETS]->(t:Target)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
          AND h.herb_name_zh = $herbName
          AND (t.symbol IN $targetSymbols
               OR toUpper(t.symbol) IN $targetSymbolsUpper
               OR t.tcm_tar_id IN $targetSymbols)
        RETURN DISTINCT
          h.herb_name_zh AS herb,
          coalesce(c.name_zh, c.name, c.inchikey) AS compound,
          c.inchikey AS inchikey,
          c.molecular_formula AS formula,
          t.symbol AS target,
          t.tcm_tar_id AS targetId,
          $topic AS semanticTopic,
          'semantic_target_anchor' AS evidenceType
        ORDER BY target, compound
        LIMIT $limit
        """
        return self._query("findHerbCompoundTargetsByTopic", "herb_compound_targets_semantic", cypher, params)

    def _semantic_ready(self) -> bool:
        if self._semantic_search_service is None:
            return False
        try:
            return self._semantic_search_service.is_ready()
        except Exception:
            return False

    def _find_disease_prescriptions_by_semantic_disease(
        self,
        disease_name: str,
        params: dict[str, Any],
    ) -> GraphToolResult:
        if not self._semantic_ready():
            return GraphToolResult.of("disease_prescriptions", [])
        disease_query = str(params.get("diseaseQuery") or "")
        semantic_query = f"{disease_name} {disease_query} disease"
        candidates = self._semantic_search_service.search(  # type: ignore[union-attr]
            semantic_query,
            self.DISEASE_SEMANTIC_TYPES,
            min(10, max(5, self._tool_limit())),
        )
        anchors = self._accepted_general_semantic_anchors(candidates)
        params["semanticDiseaseQuery"] = semantic_query
        params["semanticDiseaseCandidateAnchors"] = self._anchor_summaries(candidates, 5)
        params["semanticDiseaseAnchors"] = self._anchor_summaries(anchors, 5)
        rows: list[dict[str, Any]] = []
        routed: set[str] = set()
        for anchor in anchors[:3]:
            key = f"{anchor.entityType}:{anchor.name}"
            if key in routed:
                continue
            routed.add(key)
            rows.extend(self._annotate_semantic_rows(self._query_semantic_disease_prescriptions(anchor, 12), anchor))
        if rows:
            params["finalEvidenceLevel"] = "semantic_disease_direct_relation"
            return GraphToolResult.of("disease_prescriptions_semantic_disease", rows)
        if anchors:
            params["finalEvidenceLevel"] = "no_direct_disease_prescription_relation"
            return GraphToolResult.of("disease_prescriptions_no_direct_relation", self._no_direct_disease_prescription_rows(anchors))
        return GraphToolResult.of("disease_prescriptions", [])

    def _find_disease_prescriptions_by_semantic_fallback(
        self,
        disease_name: str,
        params: dict[str, Any],
    ) -> GraphToolResult:
        if not self._semantic_ready():
            return GraphToolResult.of("disease_prescriptions", [])
        disease_query = str(params.get("diseaseQuery") or "")
        semantic_query = f"{disease_name} {disease_query} 治疗 方剂 prescription formula"
        candidates = self._semantic_search_service.search(  # type: ignore[union-attr]
            semantic_query,
            self.DISEASE_PRESCRIPTION_FALLBACK_TYPES,
            min(20, max(8, self._tool_limit())),
        )
        anchors = self._accepted_general_semantic_anchors(candidates)
        params["semanticFallbackQuery"] = semantic_query
        params["semanticCandidateAnchors"] = self._anchor_summaries(candidates, 8)
        params["semanticAnchors"] = self._anchor_summaries(anchors, 8)
        rows: list[dict[str, Any]] = []
        routed: set[str] = set()
        for anchor in anchors[:6]:
            key = f"{anchor.entityType}:{anchor.name}"
            if key in routed:
                continue
            routed.add(key)
            rows.extend(self._route_semantic_anchor(disease_name, anchor, max(3, min(12, self._tool_limit()))))
        if rows:
            params["finalEvidenceLevel"] = "semantic_prescription_fallback"
            return GraphToolResult.of("disease_prescriptions_semantic_fallback", rows)
        return GraphToolResult.of("disease_prescriptions", [])

    def _find_prescriptions_by_semantic_condition(self, question: str, params: dict[str, Any]) -> GraphToolResult:
        if not self._semantic_ready():
            return GraphToolResult.of("semantic_condition_prescriptions", [])
        expanded = self._expand_condition_question(question)
        candidates = self._semantic_search_service.search(  # type: ignore[union-attr]
            expanded,
            self.CONDITION_PRESCRIPTION_SEMANTIC_TYPES,
            min(30, max(10, self._tool_limit())),
        )
        anchors = self._accepted_general_semantic_anchors(candidates)
        params["semanticConditionQuery"] = expanded
        params["semanticConditionCandidateAnchors"] = self._anchor_summaries(candidates, 10)
        params["semanticConditionAnchors"] = self._anchor_summaries(anchors, 10)
        rows: list[dict[str, Any]] = []
        routed: set[str] = set()
        for anchor in anchors[:8]:
            key = f"{anchor.entityType}:{anchor.name}"
            if key in routed:
                continue
            routed.add(key)
            kind = self._anchor_type(anchor)
            if kind == "disease":
                rows.extend(self._annotate_semantic_rows(self._query_semantic_disease_prescriptions(anchor, 12), anchor))
            elif kind == "syndrome":
                rows.extend(self._annotate_semantic_rows(self._query_semantic_syndrome_prescriptions(anchor, 12), anchor))
            elif kind == "tcm_symptom":
                rows.extend(self._annotate_semantic_rows(self._query_semantic_symptom_prescriptions(anchor, 12), anchor))
            elif kind == "wm_symptom":
                rows.extend(self._annotate_semantic_rows(self._query_semantic_wm_symptom_prescriptions(anchor, 12), anchor))
        if rows:
            params["finalEvidenceLevel"] = "semantic_condition_direct_relation"
            return GraphToolResult.of("semantic_condition_prescriptions", rows)
        if anchors:
            params["finalEvidenceLevel"] = "no_direct_condition_prescription_relation"
            return GraphToolResult.of("condition_prescriptions_no_direct_relation", self._no_direct_condition_prescription_rows(anchors))
        return GraphToolResult.of("semantic_condition_prescriptions", [])

    def _route_semantic_anchor(self, question: str, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        kind = self._anchor_type(anchor)
        if kind == "prescription":
            rows = self._query_semantic_prescription(anchor, limit)
        elif kind == "syndrome":
            rows = self._query_semantic_syndrome(anchor, limit)
        elif kind == "tcm_symptom":
            rows = self._query_semantic_symptom_prescriptions(anchor, limit)
        elif kind == "wm_symptom":
            rows = self._query_semantic_wm_symptom_prescriptions(anchor, limit)
        elif kind == "disease":
            rows = self._query_semantic_disease(anchor, limit)
        elif kind == "herb":
            rows = self._query_semantic_herb(anchor, limit)
        else:
            rows = [self._semantic_anchor_only_row(question, anchor)]
        return self._annotate_semantic_rows(rows, anchor)

    def _query_semantic_prescription(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        cypher = """
        MATCH (p:Prescription)
        WHERE p.name_zh = $name OR p.tcm_prescription_id = $entityId
        OPTIONAL MATCH (p)-[:CONTAINS_HERB]->(h)
        WITH p, collect(DISTINCT h.herb_name_zh)[0..12] AS herbs
        OPTIONAL MATCH (p)-[:TREATS_DISEASE]->(d:Disease)
        WITH p, herbs, collect(DISTINCT d.disease_name)[0..12] AS diseases
        RETURN DISTINCT
          'semantic_prescription' AS category,
          p.name_zh AS prescription,
          p.tcm_prescription_id AS prescriptionId,
          p.effects_zh AS effects,
          p.indications_zh AS indications,
          herbs AS herbs,
          diseases AS diseases
        LIMIT $limit
        """
        return self._graph_repository.query(cypher, self._semantic_anchor_params(anchor, limit))

    def _query_semantic_syndrome(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        cypher = """
        MATCH (s:Syndrome)
        WHERE s.syndrome_name_zh = $name OR s.tcm_syndrome_id = $entityId
        OPTIONAL MATCH (s)-[:HAS_SYMPTOM]->(sym)
        WITH s, collect(DISTINCT coalesce(sym.symptom_name_zh, sym.symptom_name))[0..12] AS symptoms
        OPTIONAL MATCH (p:Prescription)-[:TREATS_SYNDROME]->(s)
        WITH s, symptoms, collect(DISTINCT p.name_zh)[0..12] AS prescriptions
        RETURN DISTINCT
          'semantic_syndrome' AS category,
          s.syndrome_name_zh AS syndrome,
          s.tcm_syndrome_id AS syndromeId,
          symptoms AS symptoms,
          prescriptions AS prescriptions
        LIMIT $limit
        """
        return self._graph_repository.query(cypher, self._semantic_anchor_params(anchor, limit))

    def _query_semantic_disease(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        cypher = """
        MATCH (d:Disease)
        WHERE d.disease_name = $name OR d.disease_id = $entityId
        OPTIONAL MATCH (p:Prescription)-[:TREATS_DISEASE]->(d)
        WITH d, collect(DISTINCT p.name_zh)[0..12] AS prescriptions
        OPTIONAL MATCH (h)-[:TREATS_DISEASE]->(d)
        WHERE h IS NULL OR any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
        WITH d, prescriptions, collect(DISTINCT h.herb_name_zh)[0..12] AS herbs
        RETURN DISTINCT
          'semantic_disease' AS category,
          d.disease_name AS disease,
          d.disease_id AS diseaseId,
          prescriptions AS prescriptions,
          herbs AS herbs
        LIMIT $limit
        """
        return self._graph_repository.query(cypher, self._semantic_anchor_params(anchor, limit))

    def _query_semantic_herb(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        cypher = """
        MATCH (h)
        WHERE any(label IN labels(h) WHERE label IN ['Herb','CoreHerb','OtherHerb'])
          AND (h.herb_name_zh = $name OR coalesce(h.tcm_herb_id, h.tcm_herb2_id) = $entityId)
        OPTIONAL MATCH (h)-[:TREATS_SYMPTOM]->(sym)
        WITH h, collect(DISTINCT coalesce(sym.symptom_name_zh, sym.symptom_name))[0..8] AS symptoms
        OPTIONAL MATCH (h)-[:TREATS_SYNDROME]->(s:Syndrome)
        WITH h, symptoms, collect(DISTINCT s.syndrome_name_zh)[0..8] AS syndromes
        OPTIONAL MATCH (h)-[:TREATS_DISEASE]->(d:Disease)
        WITH h, symptoms, syndromes, collect(DISTINCT d.disease_name)[0..8] AS diseases
        RETURN DISTINCT
          'semantic_herb' AS category,
          h.herb_name_zh AS herb,
          coalesce(h.tcm_herb_id, h.tcm_herb2_id) AS herbId,
          h.efficacy_zh AS efficacy,
          h.indications_zh AS indications,
          symptoms AS symptoms,
          syndromes AS syndromes,
          diseases AS diseases
        LIMIT $limit
        """
        return self._graph_repository.query(cypher, self._semantic_anchor_params(anchor, limit))

    def _query_semantic_disease_prescriptions(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        cypher = """
        MATCH (d:Disease)
        WHERE d.disease_name = $name OR d.disease_id = $entityId
        MATCH (p:Prescription)-[:TREATS_DISEASE]->(d)
        RETURN DISTINCT
          'semantic_disease_prescription' AS category,
          d.disease_name AS disease,
          d.disease_id AS diseaseId,
          p.name_zh AS prescription,
          p.tcm_prescription_id AS prescriptionId,
          'semantic_disease_direct_relation' AS evidenceType
        ORDER BY disease, prescription
        LIMIT $limit
        """
        return self._graph_repository.query(cypher, self._semantic_anchor_params(anchor, limit))

    def _query_semantic_syndrome_prescriptions(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        cypher = """
        MATCH (s:Syndrome)
        WHERE s.syndrome_name_zh = $name OR s.tcm_syndrome_id = $entityId
        MATCH (p:Prescription)-[:TREATS_SYNDROME]->(s)
        RETURN DISTINCT
          'semantic_syndrome_prescription' AS category,
          s.syndrome_name_zh AS syndrome,
          s.tcm_syndrome_id AS syndromeId,
          p.name_zh AS prescription,
          p.tcm_prescription_id AS prescriptionId,
          'semantic_syndrome_direct_relation' AS evidenceType
        ORDER BY syndrome, prescription
        LIMIT $limit
        """
        return self._graph_repository.query(cypher, self._semantic_anchor_params(anchor, limit))

    def _query_semantic_symptom_prescriptions(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        cypher = """
        MATCH (sym)
        WHERE any(label IN labels(sym) WHERE label IN ['Symptom','TcmSymptom'])
          AND (sym.symptom_name_zh = $name OR sym.symptom_name = $name OR sym.tcm_symptom_id = $entityId)
        MATCH (p:Prescription)-[:TREATS_SYMPTOM]->(sym)
        RETURN DISTINCT
          'semantic_symptom_prescription' AS category,
          coalesce(sym.symptom_name_zh, sym.symptom_name) AS symptom,
          sym.tcm_symptom_id AS symptomId,
          p.name_zh AS prescription,
          p.tcm_prescription_id AS prescriptionId,
          'semantic_symptom_direct_relation' AS evidenceType
        ORDER BY symptom, prescription
        LIMIT $limit
        """
        return self._graph_repository.query(cypher, self._semantic_anchor_params(anchor, limit))

    def _query_semantic_wm_symptom_prescriptions(self, anchor: SemanticAnchor, limit: int) -> list[dict[str, Any]]:
        ids = self._metadata_string_list(anchor.metadata, "tcmSymptomIds")
        if not ids:
            return []
        cypher = """
        MATCH (sym)
        WHERE any(label IN labels(sym) WHERE label IN ['Symptom','TcmSymptom'])
          AND sym.tcm_symptom_id IN $tcmSymptomIds
        MATCH (p:Prescription)-[:TREATS_SYMPTOM]->(sym)
        RETURN DISTINCT
          'semantic_wm_symptom_prescription' AS category,
          $wmSymptom AS westernSymptom,
          $umlsId AS umlsId,
          coalesce(sym.symptom_name_zh, sym.symptom_name) AS mappedTcmSymptom,
          sym.tcm_symptom_id AS symptomId,
          p.name_zh AS prescription,
          p.tcm_prescription_id AS prescriptionId,
          'semantic_wm_symptom_mapped_direct_relation' AS evidenceType
        ORDER BY mappedTcmSymptom, prescription
        LIMIT $limit
        """
        params = self._semantic_anchor_params(anchor, limit)
        params["tcmSymptomIds"] = ids
        params["wmSymptom"] = anchor.name
        params["umlsId"] = str(anchor.metadata.get("umls_id") or "")
        return self._graph_repository.query(cypher, params)

    def _semantic_anchor_only_row(self, question: str, anchor: SemanticAnchor) -> dict[str, Any]:
        return {
            "category": "semantic_anchor",
            "question": question,
            "entityType": anchor.entityType,
            "name": anchor.name,
            "entityId": anchor.entityId,
            "targetSymbols": self._semantic_search_service.target_symbols_from_metadata(anchor.metadata)  # type: ignore[union-attr]
            if self._semantic_search_service
            else [],
            "tcmSymptomIds": self._metadata_string_list(anchor.metadata, "tcmSymptomIds"),
            "tcmSymptomNames": self._metadata_string_list(anchor.metadata, "tcmSymptomNames"),
        }

    def _annotate_semantic_rows(self, rows: list[dict[str, Any]], anchor: SemanticAnchor) -> list[dict[str, Any]]:
        annotated: list[dict[str, Any]] = []
        for row in rows or []:
            copy = {
                "semanticAnchorType": anchor.entityType,
                "semanticAnchorName": anchor.name,
                "semanticAnchorDistance": anchor.distance,
                "semanticAnchorId": anchor.entityId,
            }
            copy.update(row)
            annotated.append(copy)
        return annotated

    def _semantic_anchor_params(self, anchor: SemanticAnchor, limit: int) -> dict[str, Any]:
        return {"name": anchor.name, "entityId": anchor.entityId, "limit": max(1, limit)}

    def _accepted_general_semantic_anchors(self, anchors: list[SemanticAnchor]) -> list[SemanticAnchor]:
        return [anchor for anchor in anchors or [] if anchor.distance <= self.GENERAL_SEMANTIC_MAX_DISTANCE.get(self._anchor_type(anchor), -1)]

    def _accepted_topic_semantic_anchors(self, anchors: list[SemanticAnchor]) -> list[SemanticAnchor]:
        def max_distance(anchor: SemanticAnchor) -> float:
            kind = self._anchor_type(anchor)
            if kind == "topic":
                return self.TOPIC_MAX_DISTANCE
            if kind == "target":
                return self.TARGET_MAX_DISTANCE
            return -1

        return [anchor for anchor in anchors or [] if anchor.distance <= max_distance(anchor)]

    def _anchor_summaries(self, anchors: list[SemanticAnchor], limit: int) -> list[dict[str, Any]]:
        return [
            {"type": anchor.entityType, "name": anchor.name, "entityId": anchor.entityId, "distance": anchor.distance}
            for anchor in (anchors or [])[: max(1, limit)]
        ]

    def _anchor_type(self, anchor: SemanticAnchor) -> str:
        return (anchor.entityType or "").strip().lower()

    def _metadata_string_list(self, metadata: dict[str, Any] | None, key: str) -> list[str]:
        if not metadata:
            return []
        value = metadata.get(key)
        if isinstance(value, list):
            return [str(item).strip() for item in value if str(item).strip()]
        if isinstance(value, str):
            return [part.strip() for part in re.split(r"[,;\s]+", value) if part.strip()]
        return []

    def _no_direct_disease_prescription_rows(self, anchors: list[SemanticAnchor]) -> list[dict[str, Any]]:
        rows: list[dict[str, Any]] = []
        for anchor in anchors[:5]:
            rows.append(
                {
                    "category": "no_direct_disease_prescription_relation",
                    "disease": anchor.name,
                    "diseaseId": anchor.entityId,
                    "semanticAnchorType": anchor.entityType,
                    "semanticAnchorName": anchor.name,
                    "semanticAnchorDistance": anchor.distance,
                    "semanticAnchorId": anchor.entityId,
                    "prescriptionRelCount": 0,
                    "evidenceType": "semantic_disease_anchor_no_direct_prescription",
                    "conclusion": "知识图谱中识别到相关疾病，但未找到该疾病与方剂的直接治疗关系。",
                    "answerGuidance": "不要把语义相近的方剂表述为可治疗该疾病；可建议补充疾病-方剂关系数据。",
                }
            )
        return rows

    def _no_direct_condition_prescription_rows(self, anchors: list[SemanticAnchor]) -> list[dict[str, Any]]:
        rows: list[dict[str, Any]] = []
        for anchor in anchors[:5]:
            rows.append(
                {
                    "category": "no_direct_condition_prescription_relation",
                    "conditionType": self._anchor_type(anchor),
                    "condition": anchor.name,
                    "conditionId": anchor.entityId,
                    "semanticAnchorType": anchor.entityType,
                    "semanticAnchorName": anchor.name,
                    "semanticAnchorDistance": anchor.distance,
                    "semanticAnchorId": anchor.entityId,
                    "prescriptionRelCount": 0,
                    "evidenceType": "semantic_condition_anchor_no_direct_prescription",
                    "conclusion": "知识图谱中识别到相关条件锚点，但未找到该条件与方剂的直接治疗关系。",
                    "answerGuidance": "不要返回方剂名相似候选并表述为可治疗；应说明当前图谱没有直接关系。",
                }
            )
        return rows

    def _asks_prescription_for_condition(self, question: str | None) -> bool:
        if not question:
            return False
        normalized = re.sub(r"\s+", "", question.lower())
        asks_prescription = self._contains_any(normalized, ["方剂", "方子", "药方", "处方", "formula", "prescription"])
        asks_treatment = self._contains_any(normalized, ["治疗", "可以治", "能治", "主治", "缓解", "适用", "treat", "relieve"])
        asks_question = self._contains_any(
            normalized,
            ["用什么方剂", "用什么方子", "用什么药方", "有哪些方剂", "哪些方剂", "什么方剂", "推荐方剂", "推荐方子", "吃什么方", "开什么方", "whatformula", "whichformula"],
        )
        return asks_prescription and (asks_treatment or asks_question)

    def _expand_condition_question(self, question: str | None) -> str:
        if not question:
            return ""
        normalized = re.sub(r"\s+", "", question.lower())
        terms = [question]
        if self._contains_any(normalized, ["喉咙发炎", "嗓子发炎", "咽喉发炎", "喉咙痛", "嗓子痛", "咽痛", "咽炎", "sorethroat", "pharyngitis"]):
            terms.extend(["咽喉肿痛", "咽痛", "喉痹", "咽炎", "sore throat", "pharyngitis", "throat inflammation"])
        if self._contains_any(normalized, ["眼睛疲劳", "视疲劳", "眼疲劳", "eyestrain"]):
            terms.extend(["视疲劳", "眼干", "目涩", "eye strain", "asthenopia"])
        if self._contains_any(normalized, ["肌肉酸痛", "肌痛", "筋痛", "musclepain", "myalgia"]):
            terms.extend(["肌痛", "筋骨痛", "身痛", "muscle pain", "myalgia"])
        if self._contains_any(normalized, ["上火", "口舌生疮", "牙龈肿痛"]):
            terms.extend(["热证", "火热证", "口舌生疮", "咽喉肿痛", "牙龈肿痛"])
        return " ".join(dict.fromkeys(terms))

    def _query(
        self,
        tool_name: str,
        query_type: str,
        cypher: str,
        params: dict[str, Any],
    ) -> GraphToolResult:
        rows = self._graph_repository.query(cypher, params)
        return self._record(tool_name, params, query_type, rows)

    def _record(
        self,
        tool_name: str,
        params: dict[str, Any],
        query_type: str,
        rows: list[dict[str, Any]],
    ) -> GraphToolResult:
        result = GraphToolResult.of(query_type, rows)
        self._recorder.record(tool_name, params, result)
        return result

    def _base_params(self, key: str, value: Any) -> dict[str, Any]:
        return {key: value, "limit": self._tool_limit()}

    def _tool_limit(self) -> int:
        return max(1, self._settings.tool_query_limit)

    def _normalize_herb_names(self, herb_names: str | None) -> list[str]:
        if not herb_names:
            return []
        cleaned = re.sub(r"[\[\]{}()（）\"'“”‘’]", "", herb_names).replace("中药", "").strip()
        names: list[str] = []
        for part in re.split(r"以及|和|与|及|跟|、|,|，|;|；|/", cleaned):
            candidate = self._clean_herb_name_part(part)
            if candidate:
                normalized = self._normalizer.normalize_herb(candidate)
                if normalized and normalized not in names:
                    names.append(normalized)
        return names

    def _clean_herb_name_part(self, value: str | None) -> str:
        if not value:
            return ""
        cleaned = re.sub(r"^(药材|中草药|草药)", "", value)
        cleaned = re.sub(r"^[：:，,、。\s]+", "", cleaned)
        cleaned = re.sub(r"[：:，,、。？?\s]+$", "", cleaned)
        return cleaned.strip()

    def _curated_topic_target_symbols(self, topic: str | None) -> list[str]:
        if not topic:
            return []
        normalized = topic.lower()
        symbols: list[str] = []
        if any(key in normalized for key in ["抗炎", "炎症", "anti-inflammatory", "inflammation", "inflammatory"]):
            symbols.extend(self.ANTI_INFLAMMATION_TARGETS)
        if any(key in normalized for key in ["巨噬细胞", "单核巨噬", "吞噬细胞", "macrophage", "macrophages"]):
            symbols.extend(self.MACROPHAGE_INFLAMMATION_TARGETS)
        return list(dict.fromkeys(symbols))

    def _extract_after_any_before_any(self, text: str, markers: list[str], stops: list[str]) -> str | None:
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
        for stop in stops:
            candidate = tail.find(stop)
            if 0 <= candidate < end:
                end = candidate
        return self._clean_entity_text(tail[:end])

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
