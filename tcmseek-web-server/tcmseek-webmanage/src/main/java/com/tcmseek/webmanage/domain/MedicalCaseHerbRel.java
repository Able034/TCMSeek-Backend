package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 医案-核心中药关联对象 medical_case_herb_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class MedicalCaseHerbRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 医案ID */
    @Excel(name = "医案ID")
    private String medCaseId;

    /** 核心中药ID */
    @Excel(name = "核心中药ID")
    private String tcmHerbId;

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

    public void setMedCaseId(String medCaseId) 
    {
        this.medCaseId = medCaseId;
    }

    public String getMedCaseId() 
    {
        return medCaseId;
    }

    public void setTcmHerbId(String tcmHerbId) 
    {
        this.tcmHerbId = tcmHerbId;
    }

    public String getTcmHerbId() 
    {
        return tcmHerbId;
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
            .append("medCaseId", getMedCaseId())
            .append("tcmHerbId", getTcmHerbId())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
