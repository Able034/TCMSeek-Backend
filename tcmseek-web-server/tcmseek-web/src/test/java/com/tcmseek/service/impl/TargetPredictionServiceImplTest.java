package com.tcmseek.service.impl;

import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.config.TargetPredictionRabbitConfig;
import com.tcmseek.dao.TargetPredictionJobMapper;
import com.tcmseek.dto.TargetPredictionJobRequest;
import com.tcmseek.dto.TargetPredictionJobVO;
import com.tcmseek.pojo.entity.TargetPredictionJob;
import com.tcmseek.support.TargetPredictionConstants;
import com.tcmseek.support.TargetPredictionCacheManager;
import com.tcmseek.support.TargetPredictionModuleConfigLoader;
import com.tcmseek.support.TargetPredictionRemoteClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TargetPredictionServiceImplTest {

    @Mock
    private TargetPredictionModuleConfigLoader configLoader;
    @Mock
    private TargetPredictionJobMapper jobMapper;
    @Mock
    private TargetPredictionRemoteClient remoteClient;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private TargetPredictionCacheManager cacheManager;

    @InjectMocks
    private TargetPredictionServiceImpl service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
    }

    @Test
    void submitJob_shouldPersistAndEnqueue() throws Exception {
        TargetPredictionJobRequest request = new TargetPredictionJobRequest();
        request.setModuleId(310);
        request.setModuleName("3DSTarPred");
        request.setReferenceDatabase("ref");
        request.setQueryConformations("qc");
        request.setSimilarityThreshold("0.7");
        request.setActivityThreshold("1.0");
        request.setRanking("Top");

        MockMultipartFile sdf = new MockMultipartFile("sdfFile", "a.sdf", "application/octet-stream", "test".getBytes());

        doAnswer(invocation -> {
            TargetPredictionJob job = invocation.getArgument(0);
            job.setId(1L);
            return 1;
        }).when(jobMapper).insert(any(TargetPredictionJob.class));

        TargetPredictionJobVO vo = service.submitJob(request, sdf, "u1");

        assertNotNull(vo);
        assertEquals(1L, vo.getRequestId());
        verify(jobMapper).insert(any(TargetPredictionJob.class));
        verify(cacheManager).put(any(TargetPredictionJob.class));
        verify(rabbitTemplate).convertAndSend(
                TargetPredictionRabbitConfig.EXCHANGE,
                TargetPredictionRabbitConfig.ROUTING_SUBMIT,
                1L
        );

        // 确认文件已保存
        assertTrue(Files.list(tempDir).findAny().isPresent());
    }

    @Test
    void fetchJobResult_notCompletedShouldTriggerPoll() {
        TargetPredictionJob job = TargetPredictionJob.builder()
                .id(2L)
                .userName("u1")
                .wemolJobId(10)
                .status(TargetPredictionConstants.STATUS_RUNNING)
                .updatedAt(LocalDateTime.now().minusSeconds(30))
                .build();
        when(cacheManager.get(2L)).thenReturn(job);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.fetchJobResult(2L, "u1"));
        assertTrue(ex.getMessage().contains("作业尚未完成"));

        verify(rabbitTemplate).convertAndSend(
                TargetPredictionRabbitConfig.EXCHANGE,
                TargetPredictionRabbitConfig.ROUTING_POLL,
                2L
        );
    }
}
