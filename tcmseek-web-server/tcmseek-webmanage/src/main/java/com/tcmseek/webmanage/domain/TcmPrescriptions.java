package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * TCM方剂（含中成药）信息对象 tcm_prescriptions
 * 
 * @author Able
 * @date 2025-11-13
 */
public class TcmPrescriptions extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 方剂ID (TCMSSD59734) */
    @Excel(name = "方剂ID (TCMSSD59734)")
    private String tcmPrescriptionId;

    /** 中文名称 */
    @Excel(name = "中文名称")
    private String nameZh;

    /** 拼音名称 */
    @Excel(name = "拼音名称")
    private String pinyinName;

    /** 来源 */
    @Excel(name = "来源")
    private String source;

    /** 主治（中文） */
    @Excel(name = "主治", readConverterExp = "中=文")
    private String indicationsZh;

    /** 主治（英文） */
    @Excel(name = "主治", readConverterExp = "英=文")
    private String indicationsEn;

    /** 功效（英文） */
    @Excel(name = "功效", readConverterExp = "英=文")
    private String effects;

    /** 功效（中文） */
    @Excel(name = "功效", readConverterExp = "中=文")
    private String effectsZh;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date createdAt;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date updatedAt;

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

    public void setNameZh(String nameZh) 
    {
        this.nameZh = nameZh;
    }

    public String getNameZh() 
    {
        return nameZh;
    }

    public void setPinyinName(String pinyinName) 
    {
        this.pinyinName = pinyinName;
    }

    public String getPinyinName() 
    {
        return pinyinName;
    }

    public void setSource(String source) 
    {
        this.source = source;
    }

    public String getSource() 
    {
        return source;
    }

    public void setIndicationsZh(String indicationsZh) 
    {
        this.indicationsZh = indicationsZh;
    }

    public String getIndicationsZh() 
    {
        return indicationsZh;
    }

    public void setIndicationsEn(String indicationsEn) 
    {
        this.indicationsEn = indicationsEn;
    }

    public String getIndicationsEn() 
    {
        return indicationsEn;
    }

    public void setEffects(String effects) 
    {
        this.effects = effects;
    }

    public String getEffects() 
    {
        return effects;
    }

    public void setEffectsZh(String effectsZh) 
    {
        this.effectsZh = effectsZh;
    }

    public String getEffectsZh() 
    {
        return effectsZh;
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
            .append("tcmPrescriptionId", getTcmPrescriptionId())
            .append("nameZh", getNameZh())
            .append("pinyinName", getPinyinName())
            .append("source", getSource())
            .append("indicationsZh", getIndicationsZh())
            .append("indicationsEn", getIndicationsEn())
            .append("effects", getEffects())
            .append("effectsZh", getEffectsZh())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
