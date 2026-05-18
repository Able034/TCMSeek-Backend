package com.tcmseek.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.config.TargetPredictionRabbitConfig;
import com.tcmseek.dao.TargetPredictionJobMapper;
import com.tcmseek.dto.TargetPredictionJobRequest;
import com.tcmseek.dto.TargetPredictionJobVO;
import com.tcmseek.pojo.entity.TargetPredictionJob;
import com.tcmseek.service.TargetPredictionService;
import com.tcmseek.support.TargetPredictionConstants;
import com.tcmseek.support.TargetPredictionModuleConfigLoader;
import com.tcmseek.support.TargetPredictionRemoteClient;
import com.tcmseek.support.TargetPredictionCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TargetPredictionServiceImpl implements TargetPredictionService {

    private final TargetPredictionModuleConfigLoader configLoader;
    private final TargetPredictionJobMapper jobMapper;
    private final TargetPredictionRemoteClient remoteClient;
    private final RabbitTemplate rabbitTemplate;
    private final TargetPredictionCacheManager cacheManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${target-prediction.upload-dir:./uploads/target-prediction}")
    private String uploadDir;

    @Override
    public void login(String wemolUserName) {
        if (StringUtils.isBlank(wemolUserName)) {
            throw new ServiceException("WeMol 账号不能为空");
        }
        remoteClient.login(wemolUserName);
    }

    @Override
    public TargetPredictionJobVO submitJob(TargetPredictionJobRequest request, MultipartFile sdfFile, String wemolUserName) {
        validateSubmitArgs(request, sdfFile, wemolUserName);
        Path savedFile = saveUploadFile(sdfFile);

        TargetPredictionJob job = TargetPredictionJob.builder()
                .userName(wemolUserName)
                .moduleId(request.getModuleId())
                .moduleName(request.getModuleName())
                .requestPayloadJson(toJsonSilently(request))
                .paramsJson(toJsonSilently(buildParamSnapshot(request)))
                .uploadFileName(sdfFile.getOriginalFilename())
                .uploadPath(savedFile.toString())
                .status(TargetPredictionConstants.STATUS_PENDING)
                .resultDirUseTaskName(Boolean.TRUE.equals(request.getResultDirUseTaskName()))
                .progress(0)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        jobMapper.insert(job);
        cacheManager.put(job);

        rabbitTemplate.convertAndSend(
                TargetPredictionRabbitConfig.EXCHANGE,
                TargetPredictionRabbitConfig.ROUTING_SUBMIT,
                job.getId()
        );

        log.info("靶点预测作业已入队，jobId={}", job.getId());
        return toVO(job);
    }

    @Override
    public List<TargetPredictionJobVO> listJobs(String wemolUserName) {
        List<TargetPredictionJob> jobs = jobMapper.selectByUserName(wemolUserName);
        jobs.stream()
                .filter(this::shouldPoll)
                .forEach(job -> dispatchPoll(job.getId()));
        return jobs.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public TargetPredictionJobVO getJob(Long requestId, String wemolUserName) {
        TargetPredictionJob job = loadJob(requestId, wemolUserName);
        if (shouldPoll(job)) {
            dispatchPoll(job.getId());
        }
        return toVO(job);
    }

    @Override
    public TargetPredictionJobVO fetchJobResult(Long requestId, String wemolUserName) {
        TargetPredictionJob job = loadJob(requestId, wemolUserName);
        if (!TargetPredictionConstants.STATUS_COMPLETED.equals(job.getStatus())) {
            if (shouldPoll(job)) {
                dispatchPoll(job.getId());
            }
            throw new ServiceException("作业尚未完成，当前状态: " + job.getStatus());
        }
        return toVO(job);
    }

    @Override
    public Map<String, Object> getModuleProfile(String wemolUserName) {
        Map<String, Object> moduleInfo = remoteClient.getModuleProfile(wemolUserName);
        Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("module", moduleInfo.get("module"));
        profile.put("fetchedAt", moduleInfo.get("fetchedAt"));
        profile.put("config", configLoader.getConfig());
        return profile;
    }

    @Override
    public Path resolveResultFile(Long requestId, String relativePath, String wemolUserName) {
        TargetPredictionJob job = loadJob(requestId, wemolUserName);
        if (job.getResultDir() == null) {
            throw new ServiceException("该作业尚未生成可下载文件");
        }
        Path baseDir = Paths.get(job.getResultDir()).toAbsolutePath().normalize();
        Path target = baseDir.resolve(relativePath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new ServiceException("非法的文件路径");
        }
        if (!Files.exists(target) || Files.isDirectory(target)) {
            throw new ServiceException("文件不存在");
        }
        return target;
    }

    private void validateSubmitArgs(TargetPredictionJobRequest request, MultipartFile sdfFile, String wemolUserName) {
        if (StringUtils.isBlank(wemolUserName)) {
            throw new ServiceException("WeMol 账号不能为空");
        }
        if (sdfFile == null || sdfFile.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        if (request == null) {
            throw new ServiceException("请求参数不能为空");
        }
    }

    private Path saveUploadFile(MultipartFile sdfFile) {
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String cleanedName = Objects.requireNonNullElse(sdfFile.getOriginalFilename(), "upload.sdf");
            String fileName = UUID.randomUUID() + "_" + cleanedName;
            Path path = dir.resolve(fileName);
            sdfFile.transferTo(path);
            return path;
        } catch (IOException ex) {
            throw new ServiceException("保存上传文件失败: " + ex.getMessage());
        }
    }

    private TargetPredictionJob loadJob(Long requestId, String wemolUserName) {
        TargetPredictionJob job = cacheManager.get(requestId);
        if (job == null) {
            job = jobMapper.selectById(requestId);
            cacheManager.put(job);
        }
        if (job == null || !wemolUserName.equals(job.getUserName())) {
            throw new ServiceException("未找到对应作业");
        }
        return job;
    }

    private boolean shouldPoll(TargetPredictionJob job) {
        if (job == null) {
            return false;
        }
        if (job.getWemolJobId() == null) {
            return false;
        }
        if (TargetPredictionConstants.STATUS_COMPLETED.equals(job.getStatus())
                || TargetPredictionConstants.STATUS_FAILED.equals(job.getStatus())) {
            return false;
        }
        // 简单限流：15秒内更新过就不再推送轮询
        LocalDateTime updatedAt = job.getUpdatedAt();
        return updatedAt == null || updatedAt.isBefore(LocalDateTime.now().minusSeconds(15));
    }

    private void dispatchPoll(Long jobId) {
        rabbitTemplate.convertAndSend(
                TargetPredictionRabbitConfig.EXCHANGE,
                TargetPredictionRabbitConfig.ROUTING_POLL,
                jobId
        );
    }

    private TargetPredictionJobVO toVO(TargetPredictionJob job) {
        TargetPredictionJobVO vo = new TargetPredictionJobVO();
        vo.setJobId(job.getId());
        vo.setRequestId(job.getId()); // 保持兼容：requestId 即本地作业ID
        vo.setWemolJobId(job.getWemolJobId());
        vo.setModuleId(job.getModuleId());
        vo.setModuleName(job.getModuleName());
        vo.setWemolUserName(job.getUserName());
        vo.setParams(readMap(job.getParamsJson()));
        vo.setUploadedFileName(job.getUploadFileName());
        vo.setStatus(job.getStatus());
        vo.setLastMessage(job.getLastMessage());
        vo.setResultDir(job.getResultDir());
        vo.setResultDirUseTaskName(job.getResultDirUseTaskName());
        vo.setPredictedTargets(readList(job.getPredictedTargetsJson()));
        vo.setDetails(readList(job.getDetailsJson()));
        vo.setResultFiles(readList(job.getResultFilesJson()));
        vo.setCreatedAt(job.getCreatedAt());
        vo.setUpdatedAt(job.getUpdatedAt());
        return vo;
    }

    private Map<String, Object> buildParamSnapshot(TargetPredictionJobRequest request) {
        Map<String, Object> map = Map.of(
                "Reference Database", request.getReferenceDatabase(),
                "Query Conformations", request.getQueryConformations(),
                "Similarity Threshold", request.getSimilarityThreshold(),
                "Activity Threshold", request.getActivityThreshold(),
                "Ranking", request.getRanking()
        );
        request.setParams(map);
        return map;
    }

    private Map<String, Object> readMap(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("解析参数快照失败: {}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> readList(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("解析列表JSON失败: {}", e.getMessage());
            return null;
        }
    }

    private String toJsonSilently(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ServiceException("序列化失败: " + e.getMessage());
        }
    }
}
