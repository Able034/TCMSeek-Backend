package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 西医症状信息对象 wm_symptoms
 * 
 * @author Able
 * @date 2025-11-13
 */
public class WmSymptoms extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Long id;

    /** 西医症状ID */
    @Excel(name = "西医症状ID")
    private String wmSymptomId;

    /** 西医症状名称 */
    @Excel(name = "西医症状名称")
    private String symptomName;

    /** UMLS标识符 */
    @Excel(name = "UMLS标识符")
    private String umlsId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedAt;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setWmSymptomId(String wmSymptomId) 
    {
        this.wmSymptomId = wmSymptomId;
    }

    public String getWmSymptomId() 
    {
        return wmSymptomId;
    }

    public void setSymptomName(String symptomName) 
    {
        this.symptomName = symptomName;
    }

    public String getSymptomName() 
    {
        return symptomName;
    }

    public void setUmlsId(String umlsId) 
    {
        this.umlsId = umlsId;
    }

    public String getUmlsId() 
    {
        return umlsId;
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
            .append("wmSymptomId", getWmSymptomId())
            .append("symptomName", getSymptomName())
            .append("umlsId", getUmlsId())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
