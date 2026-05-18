package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 医案信息对象 medical_cases
 * 
 * @author Able
 * @date 2025-11-13
 */
public class MedicalCases extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 医案ID (Med_Case0001) */
    @Excel(name = "医案ID (Med_Case0001)")
    private String medCaseId;

    /** 医案全文报告 */
    @Excel(name = "医案全文报告")
    private String caseReport;

    /** 医生姓名 */
    @Excel(name = "医生姓名")
    private String physician;

    /** 中医疾病 */
    @Excel(name = "中医疾病")
    private String tcmDisease;

    /** 西医疾病 */
    @Excel(name = "西医疾病")
    private String wmDisease;

    /** 中医症状 */
    @Excel(name = "中医症状")
    private String tcmSymptoms;

    /** 西医症状 */
    @Excel(name = "西医症状")
    private String wmSymptoms;

    /** 二便情况 */
    @Excel(name = "二便情况")
    private String urinationDefecation;

    /** 脉象 */
    @Excel(name = "脉象")
    private String pulseCondition;

    /** 舌象 */
    @Excel(name = "舌象")
    private String tongueAppearance;

    /** 中医证候 */
    @Excel(name = "中医证候")
    private String tcmSyndrome;

    /** 中医治法 */
    @Excel(name = "中医治法")
    private String tcmTreatment;

    /** 使用方剂 */
    @Excel(name = "使用方剂")
    private String prescription;

    /** 中药组成 */
    @Excel(name = "中药组成")
    private String herbComposition;

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

    public void setMedCaseId(String medCaseId) 
    {
        this.medCaseId = medCaseId;
    }

    public String getMedCaseId() 
    {
        return medCaseId;
    }

    public void setCaseReport(String caseReport) 
    {
        this.caseReport = caseReport;
    }

    public String getCaseReport() 
    {
        return caseReport;
    }

    public void setPhysician(String physician) 
    {
        this.physician = physician;
    }

    public String getPhysician() 
    {
        return physician;
    }

    public void setTcmDisease(String tcmDisease) 
    {
        this.tcmDisease = tcmDisease;
    }

    public String getTcmDisease() 
    {
        return tcmDisease;
    }

    public void setWmDisease(String wmDisease) 
    {
        this.wmDisease = wmDisease;
    }

    public String getWmDisease() 
    {
        return wmDisease;
    }

    public void setTcmSymptoms(String tcmSymptoms) 
    {
        this.tcmSymptoms = tcmSymptoms;
    }

    public String getTcmSymptoms() 
    {
        return tcmSymptoms;
    }

    public void setWmSymptoms(String wmSymptoms) 
    {
        this.wmSymptoms = wmSymptoms;
    }

    public String getWmSymptoms() 
    {
        return wmSymptoms;
    }

    public void setUrinationDefecation(String urinationDefecation) 
    {
        this.urinationDefecation = urinationDefecation;
    }

    public String getUrinationDefecation() 
    {
        return urinationDefecation;
    }

    public void setPulseCondition(String pulseCondition) 
    {
        this.pulseCondition = pulseCondition;
    }

    public String getPulseCondition() 
    {
        return pulseCondition;
    }

    public void setTongueAppearance(String tongueAppearance) 
    {
        this.tongueAppearance = tongueAppearance;
    }

    public String getTongueAppearance() 
    {
        return tongueAppearance;
    }

    public void setTcmSyndrome(String tcmSyndrome) 
    {
        this.tcmSyndrome = tcmSyndrome;
    }

    public String getTcmSyndrome() 
    {
        return tcmSyndrome;
    }

    public void setTcmTreatment(String tcmTreatment) 
    {
        this.tcmTreatment = tcmTreatment;
    }

    public String getTcmTreatment() 
    {
        return tcmTreatment;
    }

    public void setPrescription(String prescription) 
    {
        this.prescription = prescription;
    }

    public String getPrescription() 
    {
        return prescription;
    }

    public void setHerbComposition(String herbComposition) 
    {
        this.herbComposition = herbComposition;
    }

    public String getHerbComposition() 
    {
        return herbComposition;
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
            .append("medCaseId", getMedCaseId())
            .append("caseReport", getCaseReport())
            .append("physician", getPhysician())
            .append("tcmDisease", getTcmDisease())
            .append("wmDisease", getWmDisease())
            .append("tcmSymptoms", getTcmSymptoms())
            .append("wmSymptoms", getWmSymptoms())
            .append("urinationDefecation", getUrinationDefecation())
            .append("pulseCondition", getPulseCondition())
            .append("tongueAppearance", getTongueAppearance())
            .append("tcmSyndrome", getTcmSyndrome())
            .append("tcmTreatment", getTcmTreatment())
            .append("prescription", getPrescription())
            .append("herbComposition", getHerbComposition())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
