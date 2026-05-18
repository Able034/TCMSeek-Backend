package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 中医证候信息对象 tcm_syndromes
 * 
 * @author Able
 * @date 2025-11-13
 */
public class TcmSyndromes extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    private Long id;

    /** 证候ID (TCM_Syndrome1) */
    @Excel(name = "证候ID (TCM_Syndrome1)")
    private String tcmSyndromeId;

    /** 证候名称（中文） */
    @Excel(name = "证候名称", readConverterExp = "中=文")
    private String syndromeNameZh;

    /** 证候名称（英文） */
    @Excel(name = "证候名称", readConverterExp = "英=文")
    private String syndromeEnglish;

    /** 拼音 */
    @Excel(name = "拼音")
    private String syndromePinyin;

    /** 证候定义（中文） */
    @Excel(name = "证候定义", readConverterExp = "中=文")
    private String syndromeDefinitionZh;

    /** 证候描述（英文） */
    @Excel(name = "证候描述", readConverterExp = "英=文")
    private String syndromeDescriptionEn;

    /** 分类（中文） */
    @Excel(name = "分类", readConverterExp = "中=文")
    private String categoryZh;

    /** 分类（英文） */
    @Excel(name = "分类", readConverterExp = "英=文")
    private String categoryEn;

    /** 数据来源 */
    @Excel(name = "数据来源")
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

    public void setTcmSyndromeId(String tcmSyndromeId) 
    {
        this.tcmSyndromeId = tcmSyndromeId;
    }

    public String getTcmSyndromeId() 
    {
        return tcmSyndromeId;
    }

    public void setSyndromeNameZh(String syndromeNameZh) 
    {
        this.syndromeNameZh = syndromeNameZh;
    }

    public String getSyndromeNameZh() 
    {
        return syndromeNameZh;
    }

    public void setSyndromeEnglish(String syndromeEnglish) 
    {
        this.syndromeEnglish = syndromeEnglish;
    }

    public String getSyndromeEnglish() 
    {
        return syndromeEnglish;
    }

    public void setSyndromePinyin(String syndromePinyin) 
    {
        this.syndromePinyin = syndromePinyin;
    }

    public String getSyndromePinyin() 
    {
        return syndromePinyin;
    }

    public void setSyndromeDefinitionZh(String syndromeDefinitionZh) 
    {
        this.syndromeDefinitionZh = syndromeDefinitionZh;
    }

    public String getSyndromeDefinitionZh() 
    {
        return syndromeDefinitionZh;
    }

    public void setSyndromeDescriptionEn(String syndromeDescriptionEn) 
    {
        this.syndromeDescriptionEn = syndromeDescriptionEn;
    }

    public String getSyndromeDescriptionEn() 
    {
        return syndromeDescriptionEn;
    }

    public void setCategoryZh(String categoryZh) 
    {
        this.categoryZh = categoryZh;
    }

    public String getCategoryZh() 
    {
        return categoryZh;
    }

    public void setCategoryEn(String categoryEn) 
    {
        this.categoryEn = categoryEn;
    }

    public String getCategoryEn() 
    {
        return categoryEn;
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
            .append("tcmSyndromeId", getTcmSyndromeId())
            .append("syndromeNameZh", getSyndromeNameZh())
            .append("syndromeEnglish", getSyndromeEnglish())
            .append("syndromePinyin", getSyndromePinyin())
            .append("syndromeDefinitionZh", getSyndromeDefinitionZh())
            .append("syndromeDescriptionEn", getSyndromeDescriptionEn())
            .append("categoryZh", getCategoryZh())
            .append("categoryEn", getCategoryEn())
            .append("source", getSource())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
