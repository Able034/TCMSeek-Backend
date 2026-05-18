package com.tcmseek.mq;

import com.tcmseek.config.TargetPredictionRabbitConfig;
import com.tcmseek.dao.TargetPredictionJobMapper;
import com.tcmseek.dto.TargetPredictionJobRequest;
import com.tcmseek.pojo.entity.TargetPredictionJob;
import com.tcmseek.support.TargetPredictionConstants;
import com.tcmseek.support.TargetPredictionRemoteClient;
import com.tcmseek.support.TargetPredictionCacheManager;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TargetPredictionSubmitConsumerTest {

    @Mock
    private TargetPredictionJobMapper jobMapper;
    @Mock
    private TargetPredictionRemoteClient remoteClient;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private TargetPredictionCacheManager cacheManager;
    @Mock
    private Channel channel;

    @InjectMocks
    private TargetPredictionSubmitConsumer consumer;

    @Test
    void handleSubmit_shouldSubmitAndEnqueuePoll() throws Exception {
        TargetPredictionJob job = TargetPredictionJob.builder()
                .id(1L)
                .userName("u1")
                .status(TargetPredictionConstants.STATUS_PENDING)
                .uploadPath(Paths.get("tmp").toString())
                .requestPayloadJson("{\"moduleId\":1,\"moduleName\":\"m\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(jobMapper.selectById(1L)).thenReturn(job);
        when(remoteClient.submitJob(any(TargetPredictionJobRequest.class), any(), eq("u1")))
                .thenReturn(Map.of("wemolJobId", 123, "status", "submitted"));

        consumer.handleSubmit(1L, channel, 11L);

        ArgumentCaptor<TargetPredictionJob> captor = ArgumentCaptor.forClass(TargetPredictionJob.class);
        verify(jobMapper).update(captor.capture());
        TargetPredictionJob updated = captor.getValue();
        assertEquals(123, updated.getWemolJobId());
        assertEquals(TargetPredictionConstants.STATUS_SUBMITTED, updated.getStatus());

        verify(cacheManager).put(any(TargetPredictionJob.class));
        verify(rabbitTemplate).convertAndSend(
                TargetPredictionRabbitConfig.EXCHANGE,
                TargetPredictionRabbitConfig.ROUTING_POLL,
                1L
        );
        verify(channel).basicAck(11L, false);
    }
}
