package com.tcmseek.pojo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphResult {

    @Builder.Default
    private Map<String, Object> centerNode = Collections.emptyMap();

    @Builder.Default
    private List<RelationshipData> relationships = Collections.emptyList();

    /**
     * 获取中心节点的ID
     */
    public String getCenterNodeId() {
        if (centerNode == null) return null;

        // 根据不同的节点类型获取ID
        if (centerNode.containsKey("tcm_herb_id")) {
            return (String) centerNode.get("tcm_herb_id");
        } else if (centerNode.containsKey("tcm_prescription_id")) {
            return (String) centerNode.get("tcm_prescription_id");
        } else if (centerNode.containsKey("inchikey")) {
            return (String) centerNode.get("inchikey");
        } else if (centerNode.containsKey("tcm_tar_id")) {
            return (String) centerNode.get("tcm_tar_id");
        } else if (centerNode.containsKey("disease_id")) {
            return (String) centerNode.get("disease_id");
        }
        return null;
    }

    /**
     * 获取中心节点的名称
     */
    public String getCenterNodeName() {
        if (centerNode == null) return null;

        if (centerNode.containsKey("herb_name_zh")) {
            return (String) centerNode.get("herb_name_zh");
        } else if (centerNode.containsKey("name_zh")) {
            return (String) centerNode.get("name_zh");
        } else if (centerNode.containsKey("symbol")) {
            return (String) centerNode.get("symbol");
        } else if (centerNode.containsKey("disease_name")) {
            return (String) centerNode.get("disease_name");
        } else if (centerNode.containsKey("syndrome_name_zh")) {
            return (String) centerNode.get("syndrome_name_zh");
        }
        return null;
    }

    /**
     * 根据关系类型筛选关系数据
     */
    public List<RelationshipData> getRelationshipsByType(String relType) {
        if (relationships == null) {
            return Collections.emptyList();
        }
        return relationships.stream()
                .filter(rel -> relType.equals(rel.getRelType()))
                .collect(Collectors.toList());
    }

    /**
     * 根据目标节点标签筛选关系数据
     */
    public List<RelationshipData> getRelationshipsByTargetLabel(String label) {
        if (relationships == null) {
            return Collections.emptyList();
        }
        return relationships.stream()
                .filter(rel -> rel.getTargetNode() != null &&
                        rel.getTargetNode().containsKey(getIdPropertyByLabel(label)))
                .collect(Collectors.toList());
    }

    /**
     * 根据标签获取对应的ID属性名
     */
    private String getIdPropertyByLabel(String label) {
        switch (label) {
            case "Herb": return "tcm_herb_id";
            case "Prescription": return "tcm_prescription_id";
            case "Compound": return "inchikey";
            case "Target": return "tcm_tar_id";
            case "Disease": return "disease_id";
            case "Symptom": return "tcm_symptom_id";
            case "Syndrome": return "tcm_syndrome_id";
            case "Phenotype": return "phenotype_id";
            case "Pathway": return "pathway_id";
            default: return "id";
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipData {
        private String relType;
        private Map<String, Object> relProps;
        private Map<String, Object> targetNode;

        /**
         * 获取目标节点的ID
         */
        public String getTargetNodeId() {
            if (targetNode == null) return null;

            // 根据不同的节点类型获取ID
            if (targetNode.containsKey("tcm_herb_id")) {
                return (String) targetNode.get("tcm_herb_id");
            } else if (targetNode.containsKey("tcm_prescription_id")) {
                return (String) targetNode.get("tcm_prescription_id");
            } else if (targetNode.containsKey("inchikey")) {
                return (String) targetNode.get("inchikey");
            } else if (targetNode.containsKey("tcm_tar_id")) {
                return (String) targetNode.get("tcm_tar_id");
            } else if (targetNode.containsKey("disease_id")) {
                return (String) targetNode.get("disease_id");
            } else if (targetNode.containsKey("tcm_symptom_id")) {
                return (String) targetNode.get("tcm_symptom_id");
            } else if (targetNode.containsKey("tcm_syndrome_id")) {
                return (String) targetNode.get("tcm_syndrome_id");
            }
            return null;
        }

        /**
         * 获取目标节点的名称
         */
        public String getTargetNodeName() {
            if (targetNode == null) return null;

            if (targetNode.containsKey("herb_name_zh")) {
                return (String) targetNode.get("herb_name_zh");
            } else if (targetNode.containsKey("name_zh")) {
                return (String) targetNode.get("name_zh");
            } else if (targetNode.containsKey("symbol")) {
                return (String) targetNode.get("symbol");
            } else if (targetNode.containsKey("disease_name")) {
                return (String) targetNode.get("disease_name");
            } else if (targetNode.containsKey("syndrome_name_zh")) {
                return (String) targetNode.get("syndrome_name_zh");
            } else if (targetNode.containsKey("symptom_name_zh")) {
                return (String) targetNode.get("symptom_name_zh");
            }
            return null;
        }

        /**
         * 获取关系属性值
         */
        public Object getRelProperty(String key) {
            return relProps != null ? relProps.get(key) : null;
        }

        /**
         * 获取目标节点属性值
         */
        public Object getTargetProperty(String key) {
            return targetNode != null ? targetNode.get(key) : null;
        }
    }
}
