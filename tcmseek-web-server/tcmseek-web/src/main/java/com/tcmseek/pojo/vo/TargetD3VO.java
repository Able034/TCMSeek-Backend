package com.tcmseek.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "靶标D3数据")
public class TargetD3VO {

    @ApiModelProperty(value = "基因符号", example = "TP53")
    private String symbol;

    @ApiModelProperty(value = "Gene Entrez ID")
    private Integer geneEntrezId;

    @ApiModelProperty(value = "Uniprot ID")
    private String uniprotId;

    @ApiModelProperty(value = "基因描述/名称")
    private String description;

    // 批量查询时用于分组的字段（不返回给前端）
    @ApiModelProperty(hidden = true)
    private String inchikey;
}
