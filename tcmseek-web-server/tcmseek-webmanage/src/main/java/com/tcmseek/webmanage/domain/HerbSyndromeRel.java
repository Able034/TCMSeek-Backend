package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 中药-证候关联对象 herb_syndrome_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class HerbSyndromeRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 证候名称 */
    @Excel(name = "证候名称")
    private String syndromeName;

    /** 中药ID */
    @Excel(name = "中药ID")
    private String tcmHerbId;

    /** 证候ID */
    @Excel(name = "证候ID")
    private String tcmSyndromeId;

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

    public void setSyndromeName(String syndromeName) 
    {
        this.syndromeName = syndromeName;
    }

    public String getSyndromeName() 
    {
        return syndromeName;
    }

    public void setTcmHerbId(String tcmHerbId) 
    {
        this.tcmHerbId = tcmHerbId;
    }

    public String getTcmHerbId() 
    {
        return tcmHerbId;
    }

    public void setTcmSyndromeId(String tcmSyndromeId) 
    {
        this.tcmSyndromeId = tcmSyndromeId;
    }

    public String getTcmSyndromeId() 
    {
        return tcmSyndromeId;
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
            .append("syndromeName", getSyndromeName())
            .append("tcmHerbId", getTcmHerbId())
            .append("tcmSyndromeId", getTcmSyndromeId())
            .append("source", getSource())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
