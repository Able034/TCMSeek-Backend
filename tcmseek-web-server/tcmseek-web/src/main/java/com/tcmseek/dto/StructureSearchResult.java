package com.tcmseek.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分子结构搜索结果
 * Molecular Structure Search Result
 */
@Data
public class StructureSearchResult {
    
    /**
     * 化合物ID
     */
    private String compoundId;
    
    /**
     * InChIKey
     */
    private String inchikey;
    
    /**
     * 化合物名称（中文）
     */
    private String name;
    
    /**
     * 化合物名称（英文）
     */
    private String nameEn;
    
    /**
     * SMILES
     */
    private String smiles;
    
    /**
     * 分子式
     */
    private String formula;
    
    /**
     * 分子量
     */
    private BigDecimal weight;
    
    /**
     * 相似度（仅用于相似性搜索）
     */
    private Double similarity;
}

