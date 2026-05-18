package com.tcmseek.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class TargetPredictionJobRequest implements Serializable {

    private Integer moduleId;

    private String moduleName;

    private Map<String, Object> params;

    private String referenceDatabase;

    private String queryConformations;

    private String similarityThreshold;

    private String activityThreshold;

    private String ranking;

    private Boolean resultDirUseTaskName;
}
