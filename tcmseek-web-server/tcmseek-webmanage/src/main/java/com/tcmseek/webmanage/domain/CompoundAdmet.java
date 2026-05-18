package com.tcmseek.webmanage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.core.domain.BaseEntity;

/**
 * 化合物ADMET预测结果对象 compound_admet
 * 
 * @author Able
 * @date 2025-11-13
 */
public class CompoundAdmet extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 序号 */
    @Excel(name = "序号")
    private Long id;

    /** 化合物InChIKey */
    @Excel(name = "化合物InChIKey")
    private String inchikey;

    /** Ames致突变性 */
    @Excel(name = "Ames致突变性")
    private Long ames;

    /** 血脑屏障通透性 */
    @Excel(name = "血脑屏障通透性")
    private Long bbbp;

    /** 生物利用度 */
    @Excel(name = "生物利用度")
    private Long bioavailability;

    /** Caco-2通透性 */
    @Excel(name = "Caco-2通透性")
    private Long caco2;

    /** 致癌性 */
    @Excel(name = "致癌性")
    private Long carcinogens;

    /** 微粒体清除率 */
    @Excel(name = "微粒体清除率")
    private Long clearanceMicrosome;

    /** 临床毒性 */
    @Excel(name = "临床毒性")
    private Long clintox;

    /** CYP1A2抑制 */
    @Excel(name = "CYP1A2抑制")
    private Long cyp1a2Inhibition;

    /** CYP2C19抑制 */
    @Excel(name = "CYP2C19抑制")
    private Long cyp2c19Inhibition;

    /** CYP2C9抑制 */
    @Excel(name = "CYP2C9抑制")
    private Long cyp2c9Inhibition;

    /** CYP2C9底物 */
    @Excel(name = "CYP2C9底物")
    private Long cyp2c9Substrate;

    /** CYP2D6抑制 */
    @Excel(name = "CYP2D6抑制")
    private Long cyp2d6Inhibition;

    /** CYP2D6底物 */
    @Excel(name = "CYP2D6底物")
    private Long cyp2d6Substrate;

    /** CYP3A4抑制 */
    @Excel(name = "CYP3A4抑制")
    private Long cyp3a4Inhibition;

    /** CYP3A4底物 */
    @Excel(name = "CYP3A4底物")
    private Long cyp3a4Substrate;

    /** 药物性肝损伤 */
    @Excel(name = "药物性肝损伤")
    private Long dili;

    /** 自由溶解度 */
    @Excel(name = "自由溶解度")
    private Long freesolv;

    /** hERG阻断 */
    @Excel(name = "hERG阻断")
    private Long hergBlockers;

    /** hERG Karim */
    @Excel(name = "hERG Karim")
    private Long hergKarim;

    /** 人体肠道吸收 */
    @Excel(name = "人体肠道吸收")
    private Long hia;

    /** 半致死量 */
    @Excel(name = "半致死量")
    private Long ld50;

    /** 亲脂性 */
    @Excel(name = "亲脂性")
    private Long lipophilicity;

    /** PAMPA渗透性 */
    @Excel(name = "PAMPA渗透性")
    private Long pampa;

    /** P-糖蛋白底物 */
    @Excel(name = "P-糖蛋白底物")
    private Long pgp;

    /** 血浆蛋白结合率 */
    @Excel(name = "血浆蛋白结合率")
    private Long ppbr;

    /** 皮肤渗透性 */
    @Excel(name = "皮肤渗透性")
    private Long skin;

    /** 溶解度 */
    @Excel(name = "溶解度")
    private Long solubility;

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

    public void setInchikey(String inchikey) 
    {
        this.inchikey = inchikey;
    }

    public String getInchikey() 
    {
        return inchikey;
    }

    public void setAmes(Long ames) 
    {
        this.ames = ames;
    }

    public Long getAmes() 
    {
        return ames;
    }

    public void setBbbp(Long bbbp) 
    {
        this.bbbp = bbbp;
    }

    public Long getBbbp() 
    {
        return bbbp;
    }

    public void setBioavailability(Long bioavailability) 
    {
        this.bioavailability = bioavailability;
    }

    public Long getBioavailability() 
    {
        return bioavailability;
    }

    public void setCaco2(Long caco2) 
    {
        this.caco2 = caco2;
    }

    public Long getCaco2() 
    {
        return caco2;
    }

    public void setCarcinogens(Long carcinogens) 
    {
        this.carcinogens = carcinogens;
    }

    public Long getCarcinogens() 
    {
        return carcinogens;
    }

    public void setClearanceMicrosome(Long clearanceMicrosome) 
    {
        this.clearanceMicrosome = clearanceMicrosome;
    }

    public Long getClearanceMicrosome() 
    {
        return clearanceMicrosome;
    }

    public void setClintox(Long clintox) 
    {
        this.clintox = clintox;
    }

    public Long getClintox() 
    {
        return clintox;
    }

    public void setCyp1a2Inhibition(Long cyp1a2Inhibition) 
    {
        this.cyp1a2Inhibition = cyp1a2Inhibition;
    }

    public Long getCyp1a2Inhibition() 
    {
        return cyp1a2Inhibition;
    }

    public void setCyp2c19Inhibition(Long cyp2c19Inhibition) 
    {
        this.cyp2c19Inhibition = cyp2c19Inhibition;
    }

    public Long getCyp2c19Inhibition() 
    {
        return cyp2c19Inhibition;
    }

    public void setCyp2c9Inhibition(Long cyp2c9Inhibition) 
    {
        this.cyp2c9Inhibition = cyp2c9Inhibition;
    }

    public Long getCyp2c9Inhibition() 
    {
        return cyp2c9Inhibition;
    }

    public void setCyp2c9Substrate(Long cyp2c9Substrate) 
    {
        this.cyp2c9Substrate = cyp2c9Substrate;
    }

    public Long getCyp2c9Substrate() 
    {
        return cyp2c9Substrate;
    }

    public void setCyp2d6Inhibition(Long cyp2d6Inhibition) 
    {
        this.cyp2d6Inhibition = cyp2d6Inhibition;
    }

    public Long getCyp2d6Inhibition() 
    {
        return cyp2d6Inhibition;
    }

    public void setCyp2d6Substrate(Long cyp2d6Substrate) 
    {
        this.cyp2d6Substrate = cyp2d6Substrate;
    }

    public Long getCyp2d6Substrate() 
    {
        return cyp2d6Substrate;
    }

    public void setCyp3a4Inhibition(Long cyp3a4Inhibition) 
    {
        this.cyp3a4Inhibition = cyp3a4Inhibition;
    }

    public Long getCyp3a4Inhibition() 
    {
        return cyp3a4Inhibition;
    }

    public void setCyp3a4Substrate(Long cyp3a4Substrate) 
    {
        this.cyp3a4Substrate = cyp3a4Substrate;
    }

    public Long getCyp3a4Substrate() 
    {
        return cyp3a4Substrate;
    }

    public void setDili(Long dili) 
    {
        this.dili = dili;
    }

    public Long getDili() 
    {
        return dili;
    }

    public void setFreesolv(Long freesolv) 
    {
        this.freesolv = freesolv;
    }

    public Long getFreesolv() 
    {
        return freesolv;
    }

    public void setHergBlockers(Long hergBlockers) 
    {
        this.hergBlockers = hergBlockers;
    }

    public Long getHergBlockers() 
    {
        return hergBlockers;
    }

    public void setHergKarim(Long hergKarim) 
    {
        this.hergKarim = hergKarim;
    }

    public Long getHergKarim() 
    {
        return hergKarim;
    }

    public void setHia(Long hia) 
    {
        this.hia = hia;
    }

    public Long getHia() 
    {
        return hia;
    }

    public void setLd50(Long ld50) 
    {
        this.ld50 = ld50;
    }

    public Long getLd50() 
    {
        return ld50;
    }

    public void setLipophilicity(Long lipophilicity) 
    {
        this.lipophilicity = lipophilicity;
    }

    public Long getLipophilicity() 
    {
        return lipophilicity;
    }

    public void setPampa(Long pampa) 
    {
        this.pampa = pampa;
    }

    public Long getPampa() 
    {
        return pampa;
    }

    public void setPgp(Long pgp) 
    {
        this.pgp = pgp;
    }

    public Long getPgp() 
    {
        return pgp;
    }

    public void setPpbr(Long ppbr) 
    {
        this.ppbr = ppbr;
    }

    public Long getPpbr() 
    {
        return ppbr;
    }

    public void setSkin(Long skin) 
    {
        this.skin = skin;
    }

    public Long getSkin() 
    {
        return skin;
    }

    public void setSolubility(Long solubility) 
    {
        this.solubility = solubility;
    }

    public Long getSolubility() 
    {
        return solubility;
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
            .append("inchikey", getInchikey())
            .append("ames", getAmes())
            .append("bbbp", getBbbp())
            .append("bioavailability", getBioavailability())
            .append("caco2", getCaco2())
            .append("carcinogens", getCarcinogens())
            .append("clearanceMicrosome", getClearanceMicrosome())
            .append("clintox", getClintox())
            .append("cyp1a2Inhibition", getCyp1a2Inhibition())
            .append("cyp2c19Inhibition", getCyp2c19Inhibition())
            .append("cyp2c9Inhibition", getCyp2c9Inhibition())
            .append("cyp2c9Substrate", getCyp2c9Substrate())
            .append("cyp2d6Inhibition", getCyp2d6Inhibition())
            .append("cyp2d6Substrate", getCyp2d6Substrate())
            .append("cyp3a4Inhibition", getCyp3a4Inhibition())
            .append("cyp3a4Substrate", getCyp3a4Substrate())
            .append("dili", getDili())
            .append("freesolv", getFreesolv())
            .append("hergBlockers", getHergBlockers())
            .append("hergKarim", getHergKarim())
            .append("hia", getHia())
            .append("ld50", getLd50())
            .append("lipophilicity", getLipophilicity())
            .append("pampa", getPampa())
            .append("pgp", getPgp())
            .append("ppbr", getPpbr())
            .append("skin", getSkin())
            .append("solubility", getSolubility())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}
