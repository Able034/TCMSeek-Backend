package com.tcmseek.webmanage.service.impl;

import com.alibaba.excel.EasyExcel;
import com.tcmseek.webmanage.domain.CompoundAdmet;
import com.tcmseek.webmanage.mapper.CompoundAdmetMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompoundAdmetServiceImplTest {

    @Mock
    private CompoundAdmetMapper compoundAdmetMapper;

    @InjectMocks
    private CompoundAdmetServiceImpl compoundAdmetService;

    @Test
    void excelinput_should_batchInsert_and_update_with_dedup() throws Exception {
        List<CompoundAdmet> excelData = new ArrayList<>();
        CompoundAdmet a1 = new CompoundAdmet();
        a1.setInchikey("AAA");
        CompoundAdmet b1 = new CompoundAdmet();
        b1.setInchikey("BBB");
        CompoundAdmet a2 = new CompoundAdmet();
        a2.setInchikey("AAA");
        excelData.add(a1);
        excelData.add(b1);
        excelData.add(a2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, CompoundAdmet.class)
                .excludeColumnFieldNames(Collections.singleton("params"))
                .sheet()
                .doWrite(excelData);
        MultipartFile file = new MockMultipartFile(
                "file",
                "admet.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );

        CompoundAdmet existed = new CompoundAdmet();
        existed.setId(10L);
        existed.setInchikey("BBB");
        Date created = new Date(0L);
        existed.setCreatedAt(created);

        when(compoundAdmetMapper.selectByInchikeys(anyList())).thenReturn(Collections.singletonList(existed));

        compoundAdmetService.excelinput(file);

        ArgumentCaptor<List<CompoundAdmet>> insertCaptor = ArgumentCaptor.forClass(List.class);
        verify(compoundAdmetMapper).batchInsertCompoundAdmet(insertCaptor.capture());
        List<CompoundAdmet> inserted = insertCaptor.getValue();
        Assertions.assertEquals(1, inserted.size());
        Assertions.assertEquals("AAA", inserted.get(0).getInchikey());

        ArgumentCaptor<CompoundAdmet> updateCaptor = ArgumentCaptor.forClass(CompoundAdmet.class);
        verify(compoundAdmetMapper).updateCompoundAdmet(updateCaptor.capture());
        CompoundAdmet updated = updateCaptor.getValue();
        Assertions.assertEquals("BBB", updated.getInchikey());
        Assertions.assertEquals(10L, updated.getId());
        Assertions.assertEquals(created, updated.getCreatedAt());
    }
}
