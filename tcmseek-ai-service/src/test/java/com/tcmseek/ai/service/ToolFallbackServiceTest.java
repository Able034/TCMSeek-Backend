package com.tcmseek.ai.service;

import com.tcmseek.ai.tools.TcmGraphTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ToolFallbackServiceTest {

    private TcmGraphTools graphTools;

    private ToolFallbackService service;

    @BeforeEach
    void setUp() {
        graphTools = mock(TcmGraphTools.class);
        service = new ToolFallbackService(graphTools);
    }

    @Test
    void routesHerbCompoundQuestion() {
        assertThat(service.tryExecute("查询人参包含哪些化合物")).isTrue();

        verify(graphTools).findHerbCompounds("人参");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesHerbCompoundTargetQuestionToCombinedTool() {
        assertThat(service.tryExecute("人参含有哪些化合物？这些化合物作用哪些靶标？")).isTrue();

        verify(graphTools).findHerbCompoundTargets("人参");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesHerbCompoundTargetQuestionWithShorterWording() {
        assertThat(service.tryExecute("人参的成分对应哪些靶点")).isTrue();

        verify(graphTools).findHerbCompoundTargets("人参");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesCommonTargetQuestionWithMultipleHerbs() {
        assertThat(service.tryExecute("人参、黄芪和甘草共同靶点有哪些？")).isTrue();

        verify(graphTools).findCommonTargets("人参，黄芪，甘草");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesCommonCompoundQuestionWithMultipleHerbs() {
        assertThat(service.tryExecute("人参和黄芪有什么共同活性成分")).isTrue();

        verify(graphTools).findCommonCompounds("人参，黄芪");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesPrescriptionCompositionQuestion() {
        assertThat(service.tryExecute("六味地黄丸由什么组成")).isTrue();

        verify(graphTools).findPrescriptionHerbs("六味地黄丸");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesPrescriptionClinicalUseQuestion() {
        assertThat(service.tryExecute("六味地黄丸主治什么")).isTrue();

        verify(graphTools).findPrescriptionClinicalUse("六味地黄丸");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesHerbClinicalUseQuestion() {
        assertThat(service.tryExecute("人参有什么功效")).isTrue();

        verify(graphTools).findHerbClinicalUse("人参");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesDiseaseTargetQuestion() {
        assertThat(service.tryExecute("糖尿病有哪些靶点")).isTrue();

        verify(graphTools).findDiseaseTargets("糖尿病");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesDiseaseHerbQuestion() {
        assertThat(service.tryExecute("糖尿病有哪些相关中药")).isTrue();

        verify(graphTools).findDiseaseHerbs("糖尿病");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesDiseasePrescriptionQuestion() {
        assertThat(service.tryExecute("糖尿病有哪些相关方剂")).isTrue();

        verify(graphTools).findDiseasePrescriptions("糖尿病");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesBroadTcmQuestionToSemanticIntent() {
        String question = "\u54ea\u4e9b\u65b9\u5242\u53ef\u4ee5\u6cbb\u7597\u4e0a\u706b";

        assertThat(service.tryExecute(question)).isTrue();

        verify(graphTools).findBySemanticIntent(question);
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesShowModelHerbPrescriptionConditionQuestionWithoutSemanticIntent() {
        assertThat(service.tryExecute("\u4eba\u53c2\u7684\u65b9\u5242\u6709\u54ea\u4e9b\u6cbb\u766b\u75eb", false)).isTrue();

        verify(graphTools).findHerbDiseasePrescriptions("\u4eba\u53c2", "\u766b\u75eb");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void showModelRoutesBroadPrescriptionQuestionToExactGraphIntent() {
        String question = "\u54ea\u4e9b\u65b9\u5242\u53ef\u4ee5\u6cbb\u7597\u4e0a\u706b";

        assertThat(service.tryExecute(question, false)).isTrue();

        verify(graphTools).findByGraphIntentExact(question);
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void showModelRoutesNaturalPrescriptionQuestionToExactGraphIntent() {
        String question = "\u611f\u5192\u4e86\u6709\u4ec0\u4e48\u4e2d\u533b\u65b9\u5242\u53ef\u4ee5\u559d";

        assertThat(service.tryExecute(question, false)).isTrue();

        verify(graphTools).findByGraphIntentExact(question);
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void routesSyndromeSymptomQuestion() {
        assertThat(service.tryExecute("气虚证候包含哪些症状")).isTrue();

        verify(graphTools).findSyndromeSymptoms("气虚证候");
        verifyNoMoreInteractions(graphTools);
    }

    @Test
    void ignoresGeneralChatWithoutGraphIntent() {
        assertThat(service.tryExecute("你好，帮我介绍一下 TCMSeek")).isFalse();

        verifyNoInteractions(graphTools);
    }
}
