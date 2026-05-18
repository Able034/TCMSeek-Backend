package com.tcmseek.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcmseek.common.annotation.DataSource;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 中医医案实体类
 * Medical Case Entity
 */
@Data
public class MedicalCase implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 自增主键 */
    private Long id;

    /** 医案ID (Med_Case0001) */
    private String medCaseId;

    /** 医案全文报告 */
    private String caseReport;

    /** 医生姓名 */
    private String physician;

    /** 中医疾病 */
    private String tcmDisease;

    /** 西医疾病 */
    private String wmDisease;

    /** 中医症状 */
    private String tcmSymptoms;

    /** 西医症状 */
    private String wmSymptoms;

    /** 二便情况 */
    private String urinationDefecation;

    /** 脉象 */
    private String pulseCondition;

    /** 舌象 */
    private String tongueAppearance;

    /** 中医证候 */
    private String tcmSyndrome;

    /** 中医治法 */
    private String tcmTreatment;

    /** 使用方剂 */
    private String prescription;

    /** 中药组成 */
    private String herbComposition;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
}








