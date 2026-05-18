package com.tcmseek.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.tcmseek.config.TargetPredictionRabbitConfig;
import com.tcmseek.dao.TargetPredictionJobMapper;
import com.tcmseek.pojo.entity.TargetPredictionJob;
import com.tcmseek.support.TargetPredictionConstants;
import com.tcmseek.support.TargetPredictionRemoteClient;
import com.tcmseek.support.TargetPredictionCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class TargetPredictionPollConsumer {

    private final TargetPredictionJobMapper jobMapper;
    private final TargetPredictionRemoteClient remoteClient;
    private final RabbitTemplate rabbitTemplate;
    private final TargetPredictionCacheManager cacheManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = TargetPredictionRabbitConfig.POLL_QUEUE, ackMode = "MANUAL")
    public void handlePoll(Long jobId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        TargetPredictionJob job = jobMapper.selectById(jobId);
        if (job == null) {
            channel.basicAck(tag, false);
            return;
        }
        if (job.getWemolJobId() == null) {
            channel.basicAck(tag, false);
            return;
        }
        if (TargetPredictionConstants.STATUS_COMPLETED.equals(job.getStatus())
                || TargetPredictionConstants.STATUS_FAILED.equals(job.getStatus())) {
            channel.basicAck(tag, false);
            return;
        }

        try {
            Map<String, Object> statusResp = remoteClient.getJobStatus(job.getWemolJobId().longValue(), job.getUserName());
            String remoteStatus = Objects.toString(statusResp.get("status"), "");
            job.setLastMessage(Objects.toString(statusResp.get("message"), remoteStatus));
            job.setUpdatedAt(LocalDateTime.now());

            if (TargetPredictionConstants.FINISHED_REMOTE_STATUS.contains(remoteStatus)) {
                handleFinished(job, remoteStatus);
            } else {
                job.setStatus(TargetPredictionConstants.STATUS_RUNNING);
                jobMapper.update(job);
                cacheManager.put(job);
                TimeUnit.SECONDS.sleep(5);
                // 轻量延迟：再次入队进行后续轮询
                rabbitTemplate.convertAndSend(
                        TargetPredictionRabbitConfig.EXCHANGE,
                        TargetPredictionRabbitConfig.ROUTING_POLL,
                        jobId
                );
            }
            channel.basicAck(tag, false);
        } catch (Exception ex) {
            handleFailure(job, ex);
            boolean requeue = job.getRetryCount() == null || job.getRetryCount() < 3;
            channel.basicNack(tag, false, requeue);
            if (!requeue) {
                log.error("靶点预测作业轮询失败且达到重试上限，jobId={}", jobId, ex);
            } else {
                log.warn("靶点预测作业轮询失败，将重试，jobId={}, retry={}", jobId, job.getRetryCount(), ex);
            }
        }
    }

    private void handleFinished(TargetPredictionJob job, String remoteStatus) throws JsonProcessingException {
        if ("Done".equalsIgnoreCase(remoteStatus)) {
            Map<String, Object> result = remoteClient.fetchResult(job.getWemolJobId().longValue(), job.getUserName());
            job.setStatus(TargetPredictionConstants.STATUS_COMPLETED);
            job.setResultDir((String) result.get("savedDir"));
            job.setPredictedTargetsJson(writeJson(result.get("predictedTargets")));
            job.setDetailsJson(writeJson(result.get("details")));
            job.setResultFilesJson(writeJson(result.get("files")));
            job.setFinishedAt(LocalDateTime.now());
        } else {
            job.setStatus(TargetPredictionConstants.STATUS_FAILED);
            job.setErrorMessage(job.getLastMessage());
            job.setFinishedAt(LocalDateTime.now());
        }
        jobMapper.update(job);
        cacheManager.put(job);
    }

    private void handleFailure(TargetPredictionJob job, Exception ex) {
        job.setRetryCount((job.getRetryCount() == null ? 0 : job.getRetryCount()) + 1);
        job.setErrorMessage(ex.getMessage());
        job.setLastMessage("poll_failed");
        job.setUpdatedAt(LocalDateTime.now());
        if (job.getRetryCount() >= 3) {
            job.setStatus(TargetPredictionConstants.STATUS_FAILED);
        }
        jobMapper.update(job);
        cacheManager.put(job);
    }

    private String writeJson(Object obj) throws JsonProcessingException {
        if (obj == null) {
            return null;
        }
        return objectMapper.writeValueAsString(obj);
    }
}
