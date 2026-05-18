package com.tcmseek.common.enums;

/**
 * 数据源
 * 
 * @author ruoyi
 */
public enum DataSourceType
{
    /**
     * 主库 - MySQL
     */
    MASTER,

    /**
     * 从库
     */
    SLAVE,
    
    /**
     * RDKit数据源 - PostgreSQL (用于化学结构搜索)
     */
    RDKIT,

    /**
     * 知识图谱数据源 - Neo4j
     */
    NEO4J
}
