package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 中药-疾病关联对象 herb_disease_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class HerbDiseaseRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 核心中药ID */
    @Excel(name = "核心中药ID")
    private String tcmHerbId;

    /** 疾病ID */
    @Excel(name = "疾病ID")
    private String diseaseId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTcmHerbId(String tcmHerbId) 
    {
        this.tcmHerbId = tcmHerbId;
    }

    public String getTcmHerbId() 
    {
        return tcmHerbId;
    }

    public void setDiseaseId(String diseaseId) 
    {
        this.diseaseId = diseaseId;
    }

    public String getDiseaseId() 
    {
        return diseaseId;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("tcmHerbId", getTcmHerbId())
            .append("diseaseId", getDiseaseId())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
