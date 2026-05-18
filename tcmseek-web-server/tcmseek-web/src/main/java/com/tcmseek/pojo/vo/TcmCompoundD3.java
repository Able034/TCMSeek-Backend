package com.tcmseek.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "图谱节点化合物信息 ")
public class TcmCompoundD3 {

    @ApiModelProperty(value = "InChIKey唯一标识")
    private String inchikey;

    @ApiModelProperty(value = "分子式")
    private String molecularFormula;
}
