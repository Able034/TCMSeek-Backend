package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 证候-中医症状关联对象 syndrome_symptom_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class SyndromeSymptomRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 证候ID */
    @Excel(name = "证候ID")
    private String tcmSyndromeId;

    /** 中医症状ID */
    @Excel(name = "中医症状ID")
    private String tcmSymptomId;

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

    public void setTcmSyndromeId(String tcmSyndromeId) 
    {
        this.tcmSyndromeId = tcmSyndromeId;
    }

    public String getTcmSyndromeId() 
    {
        return tcmSyndromeId;
    }

    public void setTcmSymptomId(String tcmSymptomId) 
    {
        this.tcmSymptomId = tcmSymptomId;
    }

    public String getTcmSymptomId() 
    {
        return tcmSymptomId;
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
            .append("tcmSyndromeId", getTcmSyndromeId())
            .append("tcmSymptomId", getTcmSymptomId())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
