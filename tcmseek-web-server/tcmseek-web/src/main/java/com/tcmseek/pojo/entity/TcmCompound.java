package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 中药化合物实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "中药化合物信息实体类")
public class TcmCompound {

    @ApiModelProperty(value = "主键ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "InChIKey唯一标识")
    private String inchikey;

    @ApiModelProperty(value = "标准SMILES")
    private String canonicalSmiles;

    @ApiModelProperty(value = "PubChem CID")
    private Long pubchemCid;

    @ApiModelProperty(value = "分子式")
    private String molecularFormula;

    @ApiModelProperty(value = "分子量")
    private Double molecularWeight;

    @ApiModelProperty(value = "精确质量")
    private Double exactMass;

    @ApiModelProperty(value = "重原子数")
    private Integer heavyAtomCount;

    @ApiModelProperty(value = "原子总数")
    private Integer numAtoms;

    @ApiModelProperty(value = "键的数量")
    private Integer numBonds;

    @ApiModelProperty(value = "环的数量")
    private Integer numRings;

    @ApiModelProperty(value = "氢键供体")
    private Integer hBondDonors;

    @ApiModelProperty(value = "氢键受体")
    private Integer hBondAcceptors;

    @ApiModelProperty(value = "LogP值")
    private Double logp;

    @ApiModelProperty(value = "拓扑极性表面积")
    private Double tpsa;

    @ApiModelProperty(value = "可旋转键")
    private Integer rotatableBonds;

    @ApiModelProperty(value = "Csp3分数")
    private Double fractionCsp3;

    @ApiModelProperty(value = "化合物名称")
    private String compoundName;
}

