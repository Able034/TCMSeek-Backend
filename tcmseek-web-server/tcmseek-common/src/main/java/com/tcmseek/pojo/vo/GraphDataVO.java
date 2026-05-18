package com.tcmseek.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识图谱数据VO
 * @author TCMSeek
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphDataVO {
    /**
     * 节点列表
     */
    private List<GraphNodeVO> nodes;
    
    /**
     * 边列表
     */
    private List<GraphEdgeVO> links;
    
    /**
     * 总节点数
     */
    private Integer nodeCount;
    
    /**
     * 总边数
     */
    private Integer edgeCount;
}








