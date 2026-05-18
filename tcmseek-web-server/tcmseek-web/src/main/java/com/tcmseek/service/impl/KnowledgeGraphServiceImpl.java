package com.tcmseek.service.impl;

import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.dao.KnowledgeGraphMapper;
import com.tcmseek.pojo.vo.GraphDataVO;
import com.tcmseek.pojo.vo.GraphEdgeVO;
import com.tcmseek.pojo.vo.GraphNodeVO;
import com.tcmseek.service.KnowledgeGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 知识图谱服务实现
 * @author TCMSeek
 */
@Slf4j
@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    @Autowired
    private KnowledgeGraphMapper knowledgeGraphMapper;

    @Override
    public GraphDataVO buildGraph(String centerType, String centerId, String[] relations) {
        List<GraphNodeVO> allNodes = new ArrayList<>();
        List<GraphEdgeVO> allEdges = new ArrayList<>();
        
        // 添加中心节点
        GraphNodeVO centerNode = getCenterNode(centerType, centerId);
        if (centerNode == null) {
            log.warn("未找到中心节点: type={}, id={}", centerType, centerId);
            return new GraphDataVO(new ArrayList<>(), new ArrayList<>(), 0, 0);
        }
        centerNode = new GraphNodeVO(
                centerNode.getId(),
                centerNode.getLabel(),
                centerNode.getType(),
                true,
                centerNode.getExtra()
        );
        allNodes.add(centerNode);
        
        // ⭐ 重要：使用从数据库查询到的实际ID，而不是用户输入的值
        String actualCenterId = centerNode.getId();
        log.info("中心节点: 输入ID={}, 实际ID={}, 名称={}", centerId, actualCenterId, centerNode.getLabel());
        
        // 将relations数组转为Set以便快速查找
        Set<String> relationSet = relations != null && relations.length > 0 
            ? new HashSet<>(Arrays.asList(relations)) 
            : new HashSet<>();
        
        // 如果没有指定关系，默认查询所有可用关系
        boolean queryAll = relationSet.isEmpty();
        
        // 根据中心节点类型构建图谱（使用实际ID）
        switch (centerType) {
            case "herb":
                buildHerbGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            case "prescription":
                buildPrescriptionGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            case "compound":
                buildCompoundGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            case "gene":
                buildGeneGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            case "disease":
                buildDiseaseGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            case "symptom":
                buildSymptomGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            case "syndrome":
                buildSyndromeGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            case "wm_symptom":
                buildWmSymptomGraph(actualCenterId, relationSet, queryAll, allNodes, allEdges);
                break;
            default:
                log.warn("未知的中心节点类型: {}", centerType);
        }
        
        // 去重节点（通过id）
        Map<String, GraphNodeVO> nodeMap = new LinkedHashMap<>();
        for (GraphNodeVO node : allNodes) {
            nodeMap.putIfAbsent(node.getId(), node);
        }
        
        List<GraphNodeVO> uniqueNodes = new ArrayList<>(nodeMap.values());
        
        log.info("图谱构建完成 - 节点: {}, 边: {}", uniqueNodes.size(), allEdges.size());
        
        return new GraphDataVO(uniqueNodes, allEdges, uniqueNodes.size(), allEdges.size());
    }



    @Override
    public AjaxResult searchCenterNodes(String centerType, String keyword, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", "%" + keyword + "%");
        params.put("offset", offset);
        params.put("limit", pageSize);
        
        List<Map<String, Object>> results;
        int total;
        
        switch (centerType) {
            case "herb":
                results = knowledgeGraphMapper.searchHerbs(params);
                total = knowledgeGraphMapper.countHerbs(params);
                break;
            case "prescription":
                results = knowledgeGraphMapper.searchPrescriptions(params);
                total = knowledgeGraphMapper.countPrescriptions(params);
                break;
            case "compound":
                results = knowledgeGraphMapper.searchCompounds(params);
                total = knowledgeGraphMapper.countCompounds(params);
                break;
            case "gene":
                results = knowledgeGraphMapper.searchGenes(params);
                total = knowledgeGraphMapper.countGenes(params);
                break;
            case "disease":
                results = knowledgeGraphMapper.searchDiseases(params);
                total = knowledgeGraphMapper.countDiseases(params);
                break;
            case "symptom":
                results = knowledgeGraphMapper.searchSymptoms(params);
                total = knowledgeGraphMapper.countSymptoms(params);
                break;
            case "syndrome":
                results = knowledgeGraphMapper.searchSyndromes(params);
                total = knowledgeGraphMapper.countSyndromes(params);
                break;
            case "wm_symptom":
                results = knowledgeGraphMapper.searchWmSymptoms(params);
                total = knowledgeGraphMapper.countWmSymptoms(params);
                break;
            default:
                return AjaxResult.error("未知的节点类型");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("rows", results);
        response.put("total", total);
        
        return AjaxResult.success(response);
    }

    /**
     * 获取中心节点信息
     */
    private GraphNodeVO getCenterNode(String centerType, String centerId) {
        Map<String, Object> nodeInfo = null;
        
        switch (centerType) {
            case "herb":
                nodeInfo = knowledgeGraphMapper.getHerbById(centerId);
                break;
            case "prescription":
                nodeInfo = knowledgeGraphMapper.getPrescriptionById(centerId);
                break;
            case "compound":
                nodeInfo = knowledgeGraphMapper.getCompoundById(centerId);
                break;
            case "gene":
                nodeInfo = knowledgeGraphMapper.getGeneById(centerId);
                break;
            case "disease":
                nodeInfo = knowledgeGraphMapper.getDiseaseById(centerId);
                break;
            case "symptom":
                nodeInfo = knowledgeGraphMapper.getSymptomById(centerId);
                break;
            case "syndrome":
                nodeInfo = knowledgeGraphMapper.getSyndromeById(centerId);
                break;
            case "wm_symptom":
                nodeInfo = knowledgeGraphMapper.getWmSymptomById(centerId);
                break;
        }
        
        if (nodeInfo == null) {
            return null;
        }
        
        String label = (String) nodeInfo.get("label");
        if (label == null) {
            label = (String) nodeInfo.getOrDefault("name", centerId);
        }

        return new GraphNodeVO(
                centerId,
                label,
                centerType,
                false,
                null
        );
    }

    /**
     * 构建中药材图谱
     */
    private void buildHerbGraph(String herbId, Set<String> relations, boolean queryAll,
                                 List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 化合物
        if (queryAll || relations.contains("compound")) {
            List<Map<String, Object>> compounds = knowledgeGraphMapper.getHerbCompounds(herbId);
            addNodesToGraph(compounds, "compound", nodes, edges, herbId);
        }
        
        // 疾病
        if (queryAll || relations.contains("disease")) {
            List<Map<String, Object>> diseases = knowledgeGraphMapper.getHerbDiseases(herbId);
            addNodesToGraph(diseases, "disease", nodes, edges, herbId);
        }
        
        // 证候
        if (queryAll || relations.contains("syndrome")) {
            List<Map<String, Object>> syndromes = knowledgeGraphMapper.getHerbSyndromes(herbId);
            addNodesToGraph(syndromes, "syndrome", nodes, edges, herbId);
        }
        
        // 症状
        if (queryAll || relations.contains("symptom")) {
            List<Map<String, Object>> symptoms = knowledgeGraphMapper.getHerbSymptoms(herbId);
            addNodesToGraph(symptoms, "symptom", nodes, edges, herbId);
        }
        
        // 方剂
        if (queryAll || relations.contains("prescription")) {
            List<Map<String, Object>> prescriptions = knowledgeGraphMapper.getHerbPrescriptions(herbId);
            addNodesToGraph(prescriptions, "prescription", nodes, edges, herbId);
        }
    }

    /**
     * 构建方剂图谱
     */
    private void buildPrescriptionGraph(String prescriptionId, Set<String> relations, boolean queryAll,
                                        List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 中药材
        if (queryAll || relations.contains("herb")) {
            List<Map<String, Object>> herbs = knowledgeGraphMapper.getPrescriptionHerbs(prescriptionId);
            addNodesToGraph(herbs, "herb", nodes, edges, prescriptionId);
        }
        
        // 疾病
        if (queryAll || relations.contains("disease")) {
            List<Map<String, Object>> diseases = knowledgeGraphMapper.getPrescriptionDiseases(prescriptionId);
            addNodesToGraph(diseases, "disease", nodes, edges, prescriptionId);
        }
        
        // 证候
        if (queryAll || relations.contains("syndrome")) {
            List<Map<String, Object>> syndromes = knowledgeGraphMapper.getPrescriptionSyndromes(prescriptionId);
            addNodesToGraph(syndromes, "syndrome", nodes, edges, prescriptionId);
        }
        
        // 症状
        if (queryAll || relations.contains("symptom")) {
            List<Map<String, Object>> symptoms = knowledgeGraphMapper.getPrescriptionSymptoms(prescriptionId);
            addNodesToGraph(symptoms, "symptom", nodes, edges, prescriptionId);
        }
    }

    /**
     * 构建化合物图谱
     */
    private void buildCompoundGraph(String inchikey, Set<String> relations, boolean queryAll,
                                     List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 中药材
        if (queryAll || relations.contains("herb")) {
            List<Map<String, Object>> herbs = knowledgeGraphMapper.getCompoundHerbs(inchikey);
            addNodesToGraph(herbs, "herb", nodes, edges, inchikey);
        }
        
        // 靶标/基因
        if (queryAll || relations.contains("gene")) {
            List<Map<String, Object>> genes = knowledgeGraphMapper.getCompoundTargets(inchikey);
            addNodesToGraph(genes, "gene", nodes, edges, inchikey);
        }
    }

    /**
     * 构建基因图谱
     */
    private void buildGeneGraph(String geneId, Set<String> relations, boolean queryAll,
                                 List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 化合物
        if (queryAll || relations.contains("compound")) {
            List<Map<String, Object>> compounds = knowledgeGraphMapper.getGeneCompounds(geneId);
            addNodesToGraph(compounds, "compound", nodes, edges, geneId);
        }
        
        // 疾病
        if (queryAll || relations.contains("disease")) {
            List<Map<String, Object>> diseases = knowledgeGraphMapper.getGeneDiseases(geneId);
            addNodesToGraph(diseases, "disease", nodes, edges, geneId);
        }
        
        // 证候
        if (queryAll || relations.contains("syndrome")) {
            List<Map<String, Object>> syndromes = knowledgeGraphMapper.getGeneSyndromes(geneId);
            addNodesToGraph(syndromes, "syndrome", nodes, edges, geneId);
        }
        
        // 通路
        if (queryAll || relations.contains("pathway")) {
            List<Map<String, Object>> pathways = knowledgeGraphMapper.getGenePathways(geneId);
            addNodesToGraph(pathways, "pathway", nodes, edges, geneId);
        }
        
        // 表型
        if (queryAll || relations.contains("phenotype")) {
            List<Map<String, Object>> phenotypes = knowledgeGraphMapper.getGenePhenotypes(geneId);
            addNodesToGraph(phenotypes, "phenotype", nodes, edges, geneId);
        }
    }

    /**
     * 构建疾病图谱
     */
    private void buildDiseaseGraph(String diseaseId, Set<String> relations, boolean queryAll,
                                    List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 基因
        if (queryAll || relations.contains("gene")) {
            List<Map<String, Object>> genes = knowledgeGraphMapper.getDiseaseGenes(diseaseId);
            addNodesToGraph(genes, "gene", nodes, edges, diseaseId);
        }
        
        // 中药材
        if (queryAll || relations.contains("herb")) {
            List<Map<String, Object>> herbs = knowledgeGraphMapper.getDiseaseHerbs(diseaseId);
            addNodesToGraph(herbs, "herb", nodes, edges, diseaseId);
        }
        
        // 证候
        if (queryAll || relations.contains("syndrome")) {
            List<Map<String, Object>> syndromes = knowledgeGraphMapper.getDiseaseSyndromes(diseaseId);
            addNodesToGraph(syndromes, "syndrome", nodes, edges, diseaseId);
        }
    }

    /**
     * 构建中医症状图谱
     */
    private void buildSymptomGraph(String symptomId, Set<String> relations, boolean queryAll,
                                    List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 方剂
        if (queryAll || relations.contains("prescription")) {
            List<Map<String, Object>> prescriptions = knowledgeGraphMapper.getSymptomPrescriptions(symptomId);
            addNodesToGraph(prescriptions, "prescription", nodes, edges, symptomId);
        }
        
        // 中药材
        if (queryAll || relations.contains("herb")) {
            List<Map<String, Object>> herbs = knowledgeGraphMapper.getSymptomHerbs(symptomId);
            addNodesToGraph(herbs, "herb", nodes, edges, symptomId);
        }
        
        // 证候
        if (queryAll || relations.contains("syndrome")) {
            List<Map<String, Object>> syndromes = knowledgeGraphMapper.getSymptomSyndromes(symptomId);
            addNodesToGraph(syndromes, "syndrome", nodes, edges, symptomId);
        }
        
        // 西医症状
        if (queryAll || relations.contains("wm_symptom")) {
            List<Map<String, Object>> wmSymptoms = knowledgeGraphMapper.getSymptomWmSymptoms(symptomId);
            addNodesToGraph(wmSymptoms, "wm_symptom", nodes, edges, symptomId);
        }
    }

    /**
     * 构建中医证候图谱
     */
    private void buildSyndromeGraph(String syndromeId, Set<String> relations, boolean queryAll,
                                     List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 方剂
        if (queryAll || relations.contains("prescription")) {
            List<Map<String, Object>> prescriptions = knowledgeGraphMapper.getSyndromePrescriptions(syndromeId);
            addNodesToGraph(prescriptions, "prescription", nodes, edges, syndromeId);
        }
        
        // 中医症状
        if (queryAll || relations.contains("symptom")) {
            List<Map<String, Object>> symptoms = knowledgeGraphMapper.getSyndromeSymptoms(syndromeId);
            addNodesToGraph(symptoms, "symptom", nodes, edges, syndromeId);
        }
        
        // 中药材
        if (queryAll || relations.contains("herb")) {
            List<Map<String, Object>> herbs = knowledgeGraphMapper.getSyndromeHerbs(syndromeId);
            addNodesToGraph(herbs, "herb", nodes, edges, syndromeId);
        }
        
        // 基因
        if (queryAll || relations.contains("gene")) {
            List<Map<String, Object>> genes = knowledgeGraphMapper.getSyndromeGenes(syndromeId);
            addNodesToGraph(genes, "gene", nodes, edges, syndromeId);
        }
    }

    /**
     * 构建西医症状图谱
     */
    private void buildWmSymptomGraph(String wmSymptomId, Set<String> relations, boolean queryAll,
                                      List<GraphNodeVO> nodes, List<GraphEdgeVO> edges) {
        // 中医症状
        if (queryAll || relations.contains("symptom")) {
            List<Map<String, Object>> tcmSymptoms = knowledgeGraphMapper.getWmSymptomTcmSymptoms(wmSymptomId);
            addNodesToGraph(tcmSymptoms, "symptom", nodes, edges, wmSymptomId);
        }
        
        // 基因
        if (queryAll || relations.contains("gene")) {
            List<Map<String, Object>> genes = knowledgeGraphMapper.getWmSymptomGenes(wmSymptomId);
            addNodesToGraph(genes, "gene", nodes, edges, wmSymptomId);
        }
    }

    /**
     * 添加节点和边到图谱
     * @param items 关联节点数据
     * @param type 节点类型
     * @param nodes 节点列表
     * @param edges 边列表
     * @param sourceId 源节点ID（中心节点的实际ID）
     */
    private void addNodesToGraph(List<Map<String, Object>> items, String type,
                                  List<GraphNodeVO> nodes, List<GraphEdgeVO> edges, String sourceId) {
        for (Map<String, Object> item : items) {
            String id = (String) item.get("id");
            String label = (String) item.get("label");
            
            if (id != null && label != null) {
                GraphNodeVO node = new GraphNodeVO(id, label, type, false, null);
                nodes.add(node);
                
                GraphEdgeVO edge = new GraphEdgeVO(sourceId, id, type);
                edges.add(edge);
            }
        }
    }
}
