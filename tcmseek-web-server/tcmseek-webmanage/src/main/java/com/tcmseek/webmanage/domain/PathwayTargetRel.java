package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 通路-靶标关联对象 pathway_target_rel
 * 
 * @author Able
 * @date 2025-11-13
 */
public class PathwayTargetRel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 通路ID */
    @Excel(name = "通路ID")
    private String pathwayId;

    /** 靶标ID */
    @Excel(name = "靶标ID")
    private String tcmTarId;

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

    public void setPathwayId(String pathwayId) 
    {
        this.pathwayId = pathwayId;
    }

    public String getPathwayId() 
    {
        return pathwayId;
    }

    public void setTcmTarId(String tcmTarId) 
    {
        this.tcmTarId = tcmTarId;
    }

    public String getTcmTarId() 
    {
        return tcmTarId;
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
            .append("pathwayId", getPathwayId())
            .append("tcmTarId", getTcmTarId())
            .append("createdAt", getCreatedAt())
            .toString();
    }
}
