package com.tcmseek.mq;

import com.tcmseek.config.TargetPredictionRabbitConfig;
import com.tcmseek.dao.TargetPredictionJobMapper;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TargetPredictionPollConsumerTest {

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
    private TargetPredictionPollConsumer consumer;

    @Test
    void handlePoll_doneShouldFetchResultAndComplete() throws Exception {
        TargetPredictionJob job = TargetPredictionJob.builder()
                .id(2L)
                .userName("u1")
                .wemolJobId(200)
                .status(TargetPredictionConstants.STATUS_SUBMITTED)
                .updatedAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(jobMapper.selectById(2L)).thenReturn(job);
        when(remoteClient.getJobStatus(200L, "u1"))
                .thenReturn(Map.of("status", "Done", "message", "ok"));
        when(remoteClient.fetchResult(200L, "u1"))
                .thenReturn(Map.of("savedDir", "/tmp/res", "predictedTargets", Map.of("a", 1)));

        consumer.handlePoll(2L, channel, 22L);

        ArgumentCaptor<TargetPredictionJob> captor = ArgumentCaptor.forClass(TargetPredictionJob.class);
        verify(jobMapper).update(captor.capture());
        TargetPredictionJob updated = captor.getValue();
        assertEquals(TargetPredictionConstants.STATUS_COMPLETED, updated.getStatus());
        assertEquals("/tmp/res", updated.getResultDir());

        verify(cacheManager).put(any(TargetPredictionJob.class));
        verify(channel).basicAck(22L, false);
        // 完成后不再重新入队轮询
        verifyNoInteractionsWithPoll();
    }

    private void verifyNoInteractionsWithPoll() {
        verify(rabbitTemplate, never()).convertAndSend(
                eq(TargetPredictionRabbitConfig.EXCHANGE),
                eq(TargetPredictionRabbitConfig.ROUTING_POLL),
                any(Object.class)
        );
    }
}
