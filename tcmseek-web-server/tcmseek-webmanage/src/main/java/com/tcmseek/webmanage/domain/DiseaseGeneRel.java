package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 疾病-基因关联对象 disease_gene_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class DiseaseGeneRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 靶标ID */
    @Excel(name = "靶标ID")
    private String tcmTarId;

    /** 疾病ID */
    @Excel(name = "疾病ID")
    private String diseaseId;

    /** 关系类型 */
    @Excel(name = "关系类型")
    private String relation;

    /** 数据来源（PMID等） */
    @Excel(name = "数据来源", readConverterExp = "P=MID等")
    private String source;

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

    public void setTcmTarId(String tcmTarId) 
    {
        this.tcmTarId = tcmTarId;
    }

    public String getTcmTarId() 
    {
        return tcmTarId;
    }

    public void setDiseaseId(String diseaseId) 
    {
        this.diseaseId = diseaseId;
    }

    public String getDiseaseId() 
    {
        return diseaseId;
    }

    public void setRelation(String relation) 
    {
        this.relation = relation;
    }

    public String getRelation() 
    {
        return relation;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("tcmTarId", getTcmTarId())
            .append("diseaseId", getDiseaseId())
            .append("relation", getRelation())
            .append("source", getSource())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
