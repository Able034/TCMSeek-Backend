package com.tcmseek.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识图谱边VO
 * @author TCMSeek
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdgeVO {
    /**
     * 源节点ID
     */
    private String source;
    
    /**
     * 目标节点ID
     */
    private String target;
    
    /**
     * 关系类型
     */
    private String relation;
}








