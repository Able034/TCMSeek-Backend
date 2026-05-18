package com.tcmseek.task;

import com.tcmseek.dao.TargetPredictionJobMapper;
import com.tcmseek.pojo.entity.TargetPredictionJob;
import com.tcmseek.support.TargetPredictionConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class TargetPredictionCleanupTask {

    private final TargetPredictionJobMapper jobMapper;

    @Value("${target-prediction.cleanup-enabled:true}")
    private boolean cleanupEnabled;

    @Value("${target-prediction.upload-dir:./uploads/target-prediction}")
    private String uploadDir;

    @Value("${target-prediction.result-dir-prefix:/opt/target-prediction-service/wemol_results}")
    private String resultDirPrefix;

    @Value("${target-prediction.upload-retention-days:7}")
    private int uploadRetentionDays;

    @Value("${target-prediction.result-retention-days:7}")
    private int resultRetentionDays;

    /**
     * 每天凌晨 3 点清理过期文件
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanup() {
        if (!cleanupEnabled) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime uploadThreshold = now.minus(uploadRetentionDays, ChronoUnit.DAYS);
        LocalDateTime resultThreshold = now.minus(resultRetentionDays, ChronoUnit.DAYS);

        List<TargetPredictionJob> jobs = jobMapper.selectByUserName(null); // 这里可替换为自定义查询获取全部
        for (TargetPredictionJob job : jobs) {
            try {
                // 只清理已结束且更新时间早于阈值的作业
                if (!isEnded(job) || isAfterThreshold(job.getUpdatedAt(), resultThreshold)) {
                    continue;
                }
                // 清理结果目录
                if (StringUtils.isNotBlank(job.getResultDir())
                        && job.getResultDir().startsWith(resultDirPrefix)) {
                    deletePathQuietly(Paths.get(job.getResultDir()));
                }
                // 清理上传文件
                if (StringUtils.isNotBlank(job.getUploadPath())
                        && job.getUploadPath().startsWith(uploadDir)
                        && !isAfterThreshold(job.getCreatedAt(), uploadThreshold)) {
                    deletePathQuietly(Paths.get(job.getUploadPath()));
                }
            } catch (Exception ex) {
                log.warn("清理作业文件失败 jobId={}, err={}", job.getId(), ex.getMessage());
            }
        }
    }

    private boolean isEnded(TargetPredictionJob job) {
        String status = job.getStatus();
        return TargetPredictionConstants.STATUS_COMPLETED.equals(status)
                || TargetPredictionConstants.STATUS_FAILED.equals(status)
                || "CANCEL".equalsIgnoreCase(status);
    }

    private boolean isAfterThreshold(LocalDateTime time, LocalDateTime threshold) {
        if (time == null) return true;
        return time.isAfter(threshold);
    }

    private void deletePathQuietly(Path path) {
        try {
            if (Files.notExists(path)) return;
            Files.walk(path)
                    .sorted((p1, p2) -> p2.getNameCount() - p1.getNameCount()) // 先删子文件
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
            log.info("已清理文件/目录: {}", path);
        } catch (IOException e) {
            log.warn("删除路径失败: {}", path);
        }
    }
}
