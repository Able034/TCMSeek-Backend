package com.tcmseek.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.tcmseek.config.TargetPredictionRabbitConfig;
import com.tcmseek.dao.TargetPredictionJobMapper;
import com.tcmseek.dto.TargetPredictionJobRequest;
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

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class TargetPredictionSubmitConsumer {

    private final TargetPredictionJobMapper jobMapper;
    private final TargetPredictionRemoteClient remoteClient;
    private final RabbitTemplate rabbitTemplate;
    private final TargetPredictionCacheManager cacheManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = TargetPredictionRabbitConfig.SUBMIT_QUEUE, ackMode = "MANUAL")
    public void handleSubmit(Long jobId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        TargetPredictionJob job = jobMapper.selectById(jobId);
        if (job == null) {
            channel.basicAck(tag, false);
            return;
        }
        if (!TargetPredictionConstants.STATUS_PENDING.equals(job.getStatus())) {
            channel.basicAck(tag, false);
            return;
        }
        try {
            TargetPredictionJobRequest request = objectMapper.readValue(job.getRequestPayloadJson(), TargetPredictionJobRequest.class);
            Map<String, Object> resp = remoteClient.submitJob(request, Paths.get(job.getUploadPath()), job.getUserName());
            Integer wemolJobId = extractInteger(resp.get("wemolJobId"));
            if (wemolJobId == null) {
                throw new IllegalStateException("WeMol 服务未返回有效的作业ID");
            }
            job.setWemolJobId(wemolJobId);
            job.setStatus(TargetPredictionConstants.STATUS_SUBMITTED);
            job.setLastMessage(Objects.toString(resp.get("status"), "submitted"));
            job.setUpdatedAt(LocalDateTime.now());
            jobMapper.update(job);
            cacheManager.put(job);

            rabbitTemplate.convertAndSend(
                    TargetPredictionRabbitConfig.EXCHANGE,
                    TargetPredictionRabbitConfig.ROUTING_POLL,
                    jobId
            );
            channel.basicAck(tag, false);
            log.info("靶点预测作业已提交到远端并入轮询队列，jobId={}, wemolJobId={}", jobId, wemolJobId);
        } catch (Exception ex) {
            handleFailure(job, ex);
            boolean requeue = job.getRetryCount() == null || job.getRetryCount() < 3;
            channel.basicNack(tag, false, requeue);
            if (!requeue) {
                log.error("靶点预测作业提交失败且达到重试上限，jobId={}", jobId, ex);
            } else {
                log.warn("靶点预测作业提交失败，将重试，jobId={}, retry={}", jobId, job.getRetryCount(), ex);
            }
        }
    }

    private void handleFailure(TargetPredictionJob job, Exception ex) {
        job.setRetryCount((job.getRetryCount() == null ? 0 : job.getRetryCount()) + 1);
        job.setErrorMessage(ex.getMessage());
        job.setLastMessage("submit_failed");
        job.setUpdatedAt(LocalDateTime.now());
        if (job.getRetryCount() >= 3) {
            job.setStatus(TargetPredictionConstants.STATUS_FAILED);
        }
        jobMapper.update(job);
        cacheManager.put(job);
    }

    private Integer extractInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
