package com.tcmseek.pojo.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识图谱节点VO
 * @author TCMSeek
 */
@ExcelIgnoreUnannotated
@ContentRowHeight(20)
@HeadRowHeight(25)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphNodeVO {
    /**
     * 节点唯一标识
     */
    @ExcelProperty("id")
    @ColumnWidth(30)
    private String id;
    
    /**
     * 节点显示名称
     */
    @ExcelProperty("name")
    @ColumnWidth(30)
    private String label;
    
    /**
     * 节点类型: herb/compound/gene/disease/syndrome/symptom/prescription/phenotype/pathway
     */
    @ExcelProperty("type")
    @ColumnWidth(30)
    private String type;
    
    /**
     * 是否为中心节点
     */
    @ExcelProperty("isCenter")
    @ColumnWidth(30)
    private Boolean isCenter;
    
    /**
     * 额外信息（可选）
     */
    private String extra;
}








