package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 中药化合物理化性质对象 tcm_compounds
 * 
 * @author Able
 * @date 2025-11-13
 */
public class TcmCompounds extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** InChIKey唯一标识 */
    @Excel(name = "InChIKey唯一标识")
    private String inchikey;

    /** 标准SMILES */
    @Excel(name = "标准SMILES")
    private String canonicalSmiles;

    /** PubChem CID */
    @Excel(name = "PubChem CID")
    private Long pubchemCid;

    /** 分子式 */
    @Excel(name = "分子式")
    private String molecularFormula;

    /** 分子量 */
    @Excel(name = "分子量")
    private Long molecularWeight;

    /** 精确质量 */
    @Excel(name = "精确质量")
    private Long exactMass;

    /** 重原子数 */
    @Excel(name = "重原子数")
    private Long heavyAtomCount;

    /** 原子总数 */
    @Excel(name = "原子总数")
    private Long numAtoms;

    /** 键的数量 */
    @Excel(name = "键的数量")
    private Long numBonds;

    /** 环的数量 */
    @Excel(name = "环的数量")
    private Long numRings;

    /** 氢键供体 */
    @Excel(name = "氢键供体")
    private Long hBondDonors;

    /** 氢键受体 */
    @Excel(name = "氢键受体")
    private Long hBondAcceptors;

    /** LogP值 */
    @Excel(name = "LogP值")
    private Long logp;

    /** 拓扑极性表面积 */
    @Excel(name = "拓扑极性表面积")
    private Long tpsa;

    /** 可旋转键 */
    @Excel(name = "可旋转键")
    private Long rotatableBonds;

    /** Csp3分数 */
    @Excel(name = "Csp3分数")
    private Long fractionCsp3;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date createdAt;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private Date updatedAt;

    /** $column.columnComment */
    @Excel(name = "${comment}", readConverterExp = "$column.readConverterExp()")
    private String compoundName;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setInchikey(String inchikey) 
    {
        this.inchikey = inchikey;
    }

    public String getInchikey() 
    {
        return inchikey;
    }

    public void setCanonicalSmiles(String canonicalSmiles) 
    {
        this.canonicalSmiles = canonicalSmiles;
    }

    public String getCanonicalSmiles() 
    {
        return canonicalSmiles;
    }

    public void setPubchemCid(Long pubchemCid) 
    {
        this.pubchemCid = pubchemCid;
    }

    public Long getPubchemCid() 
    {
        return pubchemCid;
    }

    public void setMolecularFormula(String molecularFormula) 
    {
        this.molecularFormula = molecularFormula;
    }

    public String getMolecularFormula() 
    {
        return molecularFormula;
    }

    public void setMolecularWeight(Long molecularWeight) 
    {
        this.molecularWeight = molecularWeight;
    }

    public Long getMolecularWeight() 
    {
        return molecularWeight;
    }

    public void setExactMass(Long exactMass) 
    {
        this.exactMass = exactMass;
    }

    public Long getExactMass() 
    {
        return exactMass;
    }

    public void setHeavyAtomCount(Long heavyAtomCount) 
    {
        this.heavyAtomCount = heavyAtomCount;
    }

    public Long getHeavyAtomCount() 
    {
        return heavyAtomCount;
    }

    public void setNumAtoms(Long numAtoms) 
    {
        this.numAtoms = numAtoms;
    }

    public Long getNumAtoms() 
    {
        return numAtoms;
    }

    public void setNumBonds(Long numBonds) 
    {
        this.numBonds = numBonds;
    }

    public Long getNumBonds() 
    {
        return numBonds;
    }

    public void setNumRings(Long numRings) 
    {
        this.numRings = numRings;
    }

    public Long getNumRings() 
    {
        return numRings;
    }

    public void sethBondDonors(Long hBondDonors) 
    {
        this.hBondDonors = hBondDonors;
    }

    public Long gethBondDonors() 
    {
        return hBondDonors;
    }

    public void sethBondAcceptors(Long hBondAcceptors) 
    {
        this.hBondAcceptors = hBondAcceptors;
    }

    public Long gethBondAcceptors() 
    {
        return hBondAcceptors;
    }

    public void setLogp(Long logp) 
    {
        this.logp = logp;
    }

    public Long getLogp() 
    {
        return logp;
    }

    public void setTpsa(Long tpsa) 
    {
        this.tpsa = tpsa;
    }

    public Long getTpsa() 
    {
        return tpsa;
    }

    public void setRotatableBonds(Long rotatableBonds) 
    {
        this.rotatableBonds = rotatableBonds;
    }

    public Long getRotatableBonds() 
    {
        return rotatableBonds;
    }

    public void setFractionCsp3(Long fractionCsp3) 
    {
        this.fractionCsp3 = fractionCsp3;
    }

    public Long getFractionCsp3() 
    {
        return fractionCsp3;
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

    public void setCompoundName(String compoundName) 
    {
        this.compoundName = compoundName;
    }

    public String getCompoundName() 
    {
        return compoundName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("inchikey", getInchikey())
            .append("canonicalSmiles", getCanonicalSmiles())
            .append("pubchemCid", getPubchemCid())
            .append("molecularFormula", getMolecularFormula())
            .append("molecularWeight", getMolecularWeight())
            .append("exactMass", getExactMass())
            .append("heavyAtomCount", getHeavyAtomCount())
            .append("numAtoms", getNumAtoms())
            .append("numBonds", getNumBonds())
            .append("numRings", getNumRings())
            .append("hBondDonors", gethBondDonors())
            .append("hBondAcceptors", gethBondAcceptors())
            .append("logp", getLogp())
            .append("tpsa", getTpsa())
            .append("rotatableBonds", getRotatableBonds())
            .append("fractionCsp3", getFractionCsp3())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .append("compoundName", getCompoundName())
            .toString();
    }
}
