package com.tcmseek.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 分子结构搜索请求
 * Molecular Structure Search Request
 */
@Data
public class StructureSearchRequest {
    
    /**
     * 搜索类型
     * full: 全结构匹配
     * sub: 子结构搜索
     * similarity: 相似性搜索
     */
    @NotBlank(message = "搜索类型不能为空")
    private String type;
    
    /**
     * MOL 文件内容（V2000 或 V3000 格式）
     */
    @NotBlank(message = "分子结构不能为空")
    private String molFile;
    
    /**
     * 相似度阈值（仅用于相似性搜索）
     * 范围: 0.5 - 1.0
     * 默认: 0.8
     */
    @DecimalMin(value = "0.1", message = "相似度阈值不能小于0.1")
    @DecimalMax(value = "1.0", message = "相似度阈值不能大于1.0")
    private Double threshold = 0.8;
    
    /**
     * 最大结果数量
     */
    private Integer maxResults = 1500;
}

