package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 表型信息对象 phenotypes
 * 
 * @author Able
 * @date 2025-11-13
 */
public class Phenotypes extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 表型ID (HP:0000006) */
    @Excel(name = "表型ID (HP:0000006)")
    private String phenotypeId;

    /** 表型名称 */
    @Excel(name = "表型名称")
    private String phenotypeName;

    /** 数据来源 (HPO) */
    @Excel(name = "数据来源 (HPO)")
    private String source;

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

    public void setPhenotypeId(String phenotypeId) 
    {
        this.phenotypeId = phenotypeId;
    }

    public String getPhenotypeId() 
    {
        return phenotypeId;
    }

    public void setPhenotypeName(String phenotypeName) 
    {
        this.phenotypeName = phenotypeName;
    }

    public String getPhenotypeName() 
    {
        return phenotypeName;
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
            .append("phenotypeId", getPhenotypeId())
            .append("phenotypeName", getPhenotypeName())
            .append("source", getSource())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
