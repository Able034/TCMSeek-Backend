package com.tcmseek.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 靶点预测作业表实体，用于持久化作业与轮询状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetPredictionJob {

    private Long id;

    private Integer wemolJobId;

    private String userName;

    private Integer moduleId;

    private String moduleName;

    /**
     * 原始请求参数JSON（TargetPredictionJobRequest）
     */
    private String requestPayloadJson;

    /**
     * 展示用参数快照JSON（Map）
     */
    private String paramsJson;

    private String uploadFileName;

    private String uploadPath;

    private String status;

    private String lastMessage;

    private String errorMessage;

    private String resultDir;

    private Boolean resultDirUseTaskName;

    private String predictedTargetsJson;

    private String detailsJson;

    private String resultFilesJson;

    private Integer progress;

    private Integer retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime finishedAt;
}
