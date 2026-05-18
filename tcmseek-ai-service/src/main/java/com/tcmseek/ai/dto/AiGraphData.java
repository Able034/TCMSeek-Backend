package com.tcmseek.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class AiGraphData {

    private List<GraphNode> nodes = new ArrayList<>();

    private List<GraphEdge> edges = new ArrayList<>();

    private int totalTargets;

    private int displayedTargets;

    private String entityType;

    private String entityKey;

    private List<String> herbs = new ArrayList<>();

    private String mainName;

    private String mainType;

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNode> nodes) {
        this.nodes = nodes;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdge> edges) {
        this.edges = edges;
    }

    public int getTotalTargets() {
        return totalTargets;
    }

    public void setTotalTargets(int totalTargets) {
        this.totalTargets = totalTargets;
    }

    public int getDisplayedTargets() {
        return displayedTargets;
    }

    public void setDisplayedTargets(int displayedTargets) {
        this.displayedTargets = displayedTargets;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityKey() {
        return entityKey;
    }

    public void setEntityKey(String entityKey) {
        this.entityKey = entityKey;
    }

    public List<String> getHerbs() {
        return herbs;
    }

    public void setHerbs(List<String> herbs) {
        this.herbs = herbs;
    }

    public String getMainName() {
        return mainName;
    }

    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    public String getMainType() {
        return mainType;
    }

    public void setMainType(String mainType) {
        this.mainType = mainType;
    }

    public static class GraphNode {

        private String id;

        private String label;

        private String type;

        public GraphNode() {
        }

        public GraphNode(String id, String label, String type) {
            this.id = id;
            this.label = label;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class GraphEdge {

        private String source;

        private String target;

        private String type;

        public GraphEdge() {
        }

        public GraphEdge(String source, String target, String type) {
            this.source = source;
            this.target = target;
            this.type = type;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
