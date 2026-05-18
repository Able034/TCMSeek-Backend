package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 医案-方剂关联对象 medical_case_prescription_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class MedicalCasePrescriptionRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 医案ID */
    @Excel(name = "医案ID")
    private String medCaseId;

    /** 方剂ID */
    @Excel(name = "方剂ID")
    private String tcmPrescriptionId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    /** 方剂名字 */
    @Excel(name = "方剂名字")
    private String perscriptionNameZh;

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

    public void setTcmPrescriptionId(String tcmPrescriptionId) 
    {
        this.tcmPrescriptionId = tcmPrescriptionId;
    }

    public String getTcmPrescriptionId() 
    {
        return tcmPrescriptionId;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setPerscriptionNameZh(String perscriptionNameZh) 
    {
        this.perscriptionNameZh = perscriptionNameZh;
    }

    public String getPerscriptionNameZh() 
    {
        return perscriptionNameZh;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("medCaseId", getMedCaseId())
            .append("tcmPrescriptionId", getTcmPrescriptionId())
            .append("createdAt", getCreatedAt())
            .append("perscriptionNameZh", getPerscriptionNameZh())
            .toString();
    }
}
