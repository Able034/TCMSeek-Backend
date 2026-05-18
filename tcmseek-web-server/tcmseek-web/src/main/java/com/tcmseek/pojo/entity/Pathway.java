package com.tcmseek.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 通路信息实体类
 * 对应数据库表: pathways
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pathway {
    
    /** 自增主键 */
    private Long id;
    
    /** 通路ID (KEGG ID: hsa01100) */
    private String pathwayId;
    
    /** 通路名称 */
    private String name;
    
    /** 数据来源 (KEGG) */
    private String source;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
}

