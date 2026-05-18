package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.dto.TargetPredictionJobRequest;
import com.tcmseek.dto.TargetPredictionLoginRequest;
import com.tcmseek.service.TargetPredictionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@Anonymous
@RequiredArgsConstructor
@RequestMapping("/tcmseek/tools/target-prediction")
@Api(tags = "靶点预测")
public class TargetPredictionController {

    private final TargetPredictionService targetPredictionService;

    @ApiOperation("WeMol 登录")
    @PostMapping("/login")
    public AjaxResult login(@RequestBody TargetPredictionLoginRequest request) {
        targetPredictionService.login(request.getUserName());
        return AjaxResult.success();
    }

    @ApiOperation("获取靶点预测模块配置")
    @GetMapping("/module")
    public AjaxResult getModuleProfile(@RequestHeader("X-WEMOL-USER") String wemolUserName) {
        return AjaxResult.success(targetPredictionService.getModuleProfile(wemolUserName));
    }

    @ApiOperation("提交靶点预测作业")
    @PostMapping(value = "/jobs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult submitJob(
            @RequestHeader("X-WEMOL-USER") String wemolUserName,
            @RequestPart("sdfFile") MultipartFile sdfFile,
            @RequestParam("referenceDatabase") String referenceDatabase,
            @RequestParam("queryConformations") String queryConformations,
            @RequestParam("similarityThreshold") String similarityThreshold,
            @RequestParam("activityThreshold") String activityThreshold,
            @RequestParam("ranking") String ranking
    ) {
        TargetPredictionJobRequest request = new TargetPredictionJobRequest();
        request.setModuleId(310);
        request.setModuleName("3DSTarPred (Target Prediction)");
        request.setReferenceDatabase(referenceDatabase);
        request.setQueryConformations(queryConformations);
        request.setSimilarityThreshold(similarityThreshold);
        request.setActivityThreshold(activityThreshold);
        request.setRanking(ranking);
        return AjaxResult.success(targetPredictionService.submitJob(request, sdfFile, wemolUserName));
    }

    @ApiOperation("获取靶点预测作业列表")
    @GetMapping("/jobs")
    public AjaxResult listJobs(@RequestHeader("X-WEMOL-USER") String wemolUserName) {
        return AjaxResult.success(targetPredictionService.listJobs(wemolUserName));
    }

    @ApiOperation("获取单个作业详情")
    @GetMapping("/jobs/{requestId}")
    public AjaxResult getJob(
            @PathVariable("requestId") Long requestId,
            @RequestHeader("X-WEMOL-USER") String wemolUserName
    ) {
        return AjaxResult.success(targetPredictionService.getJob(requestId, wemolUserName));
    }

    @ApiOperation("下载作业结果")
    @PostMapping("/jobs/{requestId}/result")
    public AjaxResult fetchResult(
            @PathVariable("requestId") Long requestId,
            @RequestHeader("X-WEMOL-USER") String wemolUserName
    ) {
        return AjaxResult.success(targetPredictionService.fetchJobResult(requestId, wemolUserName));
    }

    @ApiOperation("下载结果文件")
    @GetMapping("/jobs/{requestId}/files/download")
    public void downloadFile(
            @PathVariable("requestId") Long requestId,
            @RequestParam("path") String relativePath,
            @RequestHeader("X-WEMOL-USER") String wemolUserName,
            HttpServletResponse response
    ) throws IOException {
        Path file = targetPredictionService.resolveResultFile(requestId, relativePath, wemolUserName);
        String filename = file.getFileName().toString();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8)
        );
        try (ServletOutputStream os = response.getOutputStream()) {
            Files.copy(file, os);
            os.flush();
        }
    }
}
