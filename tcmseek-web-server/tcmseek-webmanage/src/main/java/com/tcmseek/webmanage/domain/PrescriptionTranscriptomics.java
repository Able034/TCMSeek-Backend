package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 方剂转录组学数据对象 prescription_transcriptomics
 * 
 * @author Able
 * @date 2025-11-13
 */
public class PrescriptionTranscriptomics extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 方剂ID */
    @Excel(name = "方剂ID")
    private String tcmPrescriptionId;

    /** 基因名称 */
    @Excel(name = "基因名称")
    private String geneName;

    /** Log2倍数变化平均值 */
    @Excel(name = "Log2倍数变化平均值")
    private Long log2FcAvg;

    /** P值 */
    @Excel(name = "P值")
    private Long pValue;

    /** 方向 (up/down) */
    @Excel(name = "方向 (up/down)")
    private String direction;

    /** 数据来源 */
    @Excel(name = "数据来源")
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

    public void setTcmPrescriptionId(String tcmPrescriptionId) 
    {
        this.tcmPrescriptionId = tcmPrescriptionId;
    }

    public String getTcmPrescriptionId() 
    {
        return tcmPrescriptionId;
    }

    public void setGeneName(String geneName) 
    {
        this.geneName = geneName;
    }

    public String getGeneName() 
    {
        return geneName;
    }

    public void setLog2FcAvg(Long log2FcAvg) 
    {
        this.log2FcAvg = log2FcAvg;
    }

    public Long getLog2FcAvg() 
    {
        return log2FcAvg;
    }

    public void setpValue(Long pValue) 
    {
        this.pValue = pValue;
    }

    public Long getpValue() 
    {
        return pValue;
    }

    public void setDirection(String direction) 
    {
        this.direction = direction;
    }

    public String getDirection() 
    {
        return direction;
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
            .append("tcmPrescriptionId", getTcmPrescriptionId())
            .append("geneName", getGeneName())
            .append("log2FcAvg", getLog2FcAvg())
            .append("pValue", getpValue())
            .append("direction", getDirection())
            .append("source", getSource())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
