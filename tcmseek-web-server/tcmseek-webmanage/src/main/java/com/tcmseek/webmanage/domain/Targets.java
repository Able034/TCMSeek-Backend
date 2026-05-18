package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 靶标/基因信息对象 targets
 * 
 * @author Able
 * @date 2025-11-13
 */
public class Targets extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 靶标ID (Target_1) */
    @Excel(name = "靶标ID (Target_1)")
    private String tcmTarId;

    /** gene_entrez_id */
    @Excel(name = "gene_entrez_id")
    private Long geneEntrezId;

    /** symbol */
    @Excel(name = "symbol")
    private String symbol;

    /** Uniprot ID */
    @Excel(name = "Uniprot ID")
    private String uniprotId;

    /** Ensembl ID */
    @Excel(name = "Ensembl ID")
    private String ensemblId;

    /** 基因描述 */
    @Excel(name = "基因描述")
    private String description;

    /** 基因类型 */
    @Excel(name = "基因类型")
    private String typeOfGene;

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

    public void setTcmTarId(String tcmTarId) 
    {
        this.tcmTarId = tcmTarId;
    }

    public String getTcmTarId() 
    {
        return tcmTarId;
    }

    public void setGeneEntrezId(Long geneEntrezId) 
    {
        this.geneEntrezId = geneEntrezId;
    }

    public Long getGeneEntrezId() 
    {
        return geneEntrezId;
    }

    public void setSymbol(String symbol) 
    {
        this.symbol = symbol;
    }

    public String getSymbol() 
    {
        return symbol;
    }

    public void setUniprotId(String uniprotId) 
    {
        this.uniprotId = uniprotId;
    }

    public String getUniprotId() 
    {
        return uniprotId;
    }

    public void setEnsemblId(String ensemblId) 
    {
        this.ensemblId = ensemblId;
    }

    public String getEnsemblId() 
    {
        return ensemblId;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setTypeOfGene(String typeOfGene) 
    {
        this.typeOfGene = typeOfGene;
    }

    public String getTypeOfGene() 
    {
        return typeOfGene;
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
            .append("tcmTarId", getTcmTarId())
            .append("geneEntrezId", getGeneEntrezId())
            .append("symbol", getSymbol())
            .append("uniprotId", getUniprotId())
            .append("ensemblId", getEnsemblId())
            .append("description", getDescription())
            .append("typeOfGene", getTypeOfGene())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
