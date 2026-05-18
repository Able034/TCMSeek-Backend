package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 方剂-证候关联对象 prescription_syndrome_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class PrescriptionSyndromeRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 方剂名称 */
    @Excel(name = "方剂名称")
    private String prescriptionName;

    /** 方剂ID */
    @Excel(name = "方剂ID")
    private String tcmPrescriptionId;

    /** 证候ID */
    @Excel(name = "证候ID")
    private String tcmSyndromeId;

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

    public void setPrescriptionName(String prescriptionName) 
    {
        this.prescriptionName = prescriptionName;
    }

    public String getPrescriptionName() 
    {
        return prescriptionName;
    }

    public void setTcmPrescriptionId(String tcmPrescriptionId) 
    {
        this.tcmPrescriptionId = tcmPrescriptionId;
    }

    public String getTcmPrescriptionId() 
    {
        return tcmPrescriptionId;
    }

    public void setTcmSyndromeId(String tcmSyndromeId) 
    {
        this.tcmSyndromeId = tcmSyndromeId;
    }

    public String getTcmSyndromeId() 
    {
        return tcmSyndromeId;
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
            .append("prescriptionName", getPrescriptionName())
            .append("tcmPrescriptionId", getTcmPrescriptionId())
            .append("tcmSyndromeId", getTcmSyndromeId())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
