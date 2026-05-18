package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 中医症状信息对象 tcm_symptoms
 * 
 * @author Able
 * @date 2025-11-13
 */
public class TcmSymptoms extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 中医症状ID (TCM_Symptom1) */
    @Excel(name = "中医症状ID (TCM_Symptom1)")
    private String tcmSymptomId;

    /** 症状名称（中文） */
    @Excel(name = "症状名称", readConverterExp = "中=文")
    private String symptomNameZh;

    /** 拼音 */
    @Excel(name = "拼音")
    private String symptomPinyin;

    /** 症状定义 */
    @Excel(name = "症状定义")
    private String symptomDefinition;

    /** 症状部位 */
    @Excel(name = "症状部位")
    private String symptomLocus;

    /** 症状属性 */
    @Excel(name = "症状属性")
    private String symptomProperty;

    /** 类型 (Ontological/Synonymous) */
    @Excel(name = "类型 (Ontological/Synonymous)")
    private String type;

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

    public void setTcmSymptomId(String tcmSymptomId) 
    {
        this.tcmSymptomId = tcmSymptomId;
    }

    public String getTcmSymptomId() 
    {
        return tcmSymptomId;
    }

    public void setSymptomNameZh(String symptomNameZh) 
    {
        this.symptomNameZh = symptomNameZh;
    }

    public String getSymptomNameZh() 
    {
        return symptomNameZh;
    }

    public void setSymptomPinyin(String symptomPinyin) 
    {
        this.symptomPinyin = symptomPinyin;
    }

    public String getSymptomPinyin() 
    {
        return symptomPinyin;
    }

    public void setSymptomDefinition(String symptomDefinition) 
    {
        this.symptomDefinition = symptomDefinition;
    }

    public String getSymptomDefinition() 
    {
        return symptomDefinition;
    }

    public void setSymptomLocus(String symptomLocus) 
    {
        this.symptomLocus = symptomLocus;
    }

    public String getSymptomLocus() 
    {
        return symptomLocus;
    }

    public void setSymptomProperty(String symptomProperty) 
    {
        this.symptomProperty = symptomProperty;
    }

    public String getSymptomProperty() 
    {
        return symptomProperty;
    }

    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
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
            .append("tcmSymptomId", getTcmSymptomId())
            .append("symptomNameZh", getSymptomNameZh())
            .append("symptomPinyin", getSymptomPinyin())
            .append("symptomDefinition", getSymptomDefinition())
            .append("symptomLocus", getSymptomLocus())
            .append("symptomProperty", getSymptomProperty())
            .append("type", getType())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
