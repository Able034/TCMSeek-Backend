package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 疾病信息对象 diseases
 * 
 * @author Able
 * @date 2025-11-13
 */
public class Diseases extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 疾病ID (DOID:11832) */
    @Excel(name = "疾病ID (DOID:11832)")
    private String diseaseId;

    /** 疾病名称 */
    @Excel(name = "疾病名称")
    private String diseaseName;

    /** 数据来源 */
    @Excel(name = "数据来源")
    private String source;

    /** $column.columnComment */
    @Excel(name = "创建时间")
    private Date createdAt;

    /** $column.columnComment */
    @Excel(name = "更新时间")
    private Date updatedAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setDiseaseId(String diseaseId) 
    {
        this.diseaseId = diseaseId;
    }

    public String getDiseaseId() 
    {
        return diseaseId;
    }

    public void setDiseaseName(String diseaseName) 
    {
        this.diseaseName = diseaseName;
    }

    public String getDiseaseName() 
    {
        return diseaseName;
    }

    public void setSource(String source) 
    {
        this.source = source;
    }

    public String getSource() 
    {
        return source;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("diseaseId", getDiseaseId())
            .append("diseaseName", getDiseaseName())
            .append("source", getSource())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
