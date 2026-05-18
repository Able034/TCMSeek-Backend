package com.tcmseek.service;

import com.tcmseek.dto.TargetPredictionJobRequest;
import com.tcmseek.dto.TargetPredictionJobVO;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface TargetPredictionService {

    void login(String wemolUserName);

    TargetPredictionJobVO submitJob(TargetPredictionJobRequest request, MultipartFile sdfFile, String wemolUserName);

    List<TargetPredictionJobVO> listJobs(String wemolUserName);

    TargetPredictionJobVO getJob(Long requestId, String wemolUserName);

    TargetPredictionJobVO fetchJobResult(Long requestId, String wemolUserName);

    Map<String, Object> getModuleProfile(String wemolUserName);

    Path resolveResultFile(Long requestId, String relativePath, String wemolUserName);
}
