package com.tcmseek.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TargetPredictionJobVO implements Serializable {

    /**
     * 本地作业ID（数据库主键），前端查询/下载使用
     */
    private Long jobId;

    /**
     * 兼容旧字段，等同于 jobId
     */
    private Long requestId;

    private Integer wemolJobId;

    private Integer moduleId;

    private String moduleName;

    private String wemolUserName;

    private Map<String, Object> params;

    private String uploadedFileName;

    private String status;

    private String lastMessage;

    private String resultDir;

    private Boolean resultDirUseTaskName;

    private List<Map<String, Object>> predictedTargets;

    private List<Map<String, Object>> details;

    private List<Map<String, Object>> resultFiles;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
