package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 中药-化合物关联对象 herb_compound_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class HerbCompoundRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 中药ID */
    @Excel(name = "中药ID")
    private String tcmHerbId;

    /** 化合物InChIKey */
    @Excel(name = "化合物InChIKey")
    private String inchikey;

    /** 注释 (Blood ingredients等) */
    @Excel(name = "注释 (Blood ingredients等)")
    private String annotation;

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

    public void setTcmHerbId(String tcmHerbId) 
    {
        this.tcmHerbId = tcmHerbId;
    }

    public String getTcmHerbId() 
    {
        return tcmHerbId;
    }

    public void setInchikey(String inchikey) 
    {
        this.inchikey = inchikey;
    }

    public String getInchikey() 
    {
        return inchikey;
    }

    public void setAnnotation(String annotation) 
    {
        this.annotation = annotation;
    }

    public String getAnnotation() 
    {
        return annotation;
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
            .append("tcmHerbId", getTcmHerbId())
            .append("inchikey", getInchikey())
            .append("annotation", getAnnotation())
            .append("source", getSource())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
