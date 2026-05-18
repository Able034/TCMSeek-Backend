package com.tcmseek.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 靶标/基因实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "靶标/基因信息实体类")
public class Target {

    @ApiModelProperty(value = "主键ID")
    private Integer id;

    @ApiModelProperty(value = "靶标ID", example = "Target_1")
    private String tcmTarId;

    @ApiModelProperty(value = "Gene Entrez ID")
    private Integer geneEntrezId;

    @ApiModelProperty(value = "基因符号", example = "TP53")
    private String symbol;

    @ApiModelProperty(value = "Uniprot ID")
    private String uniprotId;

    @ApiModelProperty(value = "Ensembl ID")
    private String ensemblId;

    @ApiModelProperty(value = "基因描述")
    private String description;

    @ApiModelProperty(value = "基因类型")
    private String typeOfGene;
}

