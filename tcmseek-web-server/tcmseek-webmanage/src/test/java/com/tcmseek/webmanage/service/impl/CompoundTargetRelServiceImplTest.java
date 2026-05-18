package com.tcmseek.webmanage.service.impl;

import com.tcmseek.common.annotation.Excel;
import com.tcmseek.webmanage.domain.CompoundTargetRel;
import com.tcmseek.webmanage.mapper.CompoundTargetRelMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
class CompoundTargetRelServiceImplTest {

    @Mock
    private CompoundTargetRelMapper compoundTargetRelMapper;

    @InjectMocks
    private CompoundTargetRelServiceImpl compoundTargetRelService;

    @Test
    void excelinput_should_insert_and_update_by_composite_key() throws Exception {
        List<CompoundTargetRel> excelData = new ArrayList<>();
        CompoundTargetRel first = new CompoundTargetRel();
        first.setInchikey("AAA");
        first.setTcmTarId("T1");
        first.setSource("S1");

        CompoundTargetRel second = new CompoundTargetRel();
        second.setInchikey("BBB");
        second.setTcmTarId("T2");
        second.setSource("S2");

        // duplicate of first (should be deduped inside excel)
        CompoundTargetRel duplicate = new CompoundTargetRel();
        duplicate.setInchikey("AAA");
        duplicate.setTcmTarId("T1");
        excelData.add(first);
        excelData.add(second);
        excelData.add(duplicate);

        byte[] excelBytes = buildExcelBytes(excelData);
        MultipartFile file = new MockMultipartFile(
                "file",
                "compound_target_rel.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        CompoundTargetRel existed = new CompoundTargetRel();
        existed.setId(99L);
        existed.setInchikey("BBB");
        existed.setTcmTarId("T2");
        Date created = new Date(0L);
        existed.setCreatedAt(created);
        when(compoundTargetRelMapper.selectByCompositeKeys(anyList())).thenReturn(Collections.singletonList(existed));

        compoundTargetRelService.excelinput(file);

        ArgumentCaptor<List<CompoundTargetRel>> insertCaptor = ArgumentCaptor.forClass(List.class);
        verify(compoundTargetRelMapper).batchInsertCompoundTargetRel(insertCaptor.capture());
        List<CompoundTargetRel> inserted = insertCaptor.getValue();
        Assertions.assertEquals(1, inserted.size());
        Assertions.assertEquals("AAA", inserted.get(0).getInchikey());
        Assertions.assertEquals("T1", inserted.get(0).getTcmTarId());

        ArgumentCaptor<CompoundTargetRel> updateCaptor = ArgumentCaptor.forClass(CompoundTargetRel.class);
        verify(compoundTargetRelMapper).updateCompoundTargetRel(updateCaptor.capture());
        CompoundTargetRel updated = updateCaptor.getValue();
        Assertions.assertEquals("BBB", updated.getInchikey());
        Assertions.assertEquals("T2", updated.getTcmTarId());
        Assertions.assertEquals(99L, updated.getId());
        Assertions.assertEquals(created, updated.getCreatedAt());
    }

    /**
     * 构造和 @Excel 注解表头一致的 Excel 内容，确保 importExcel 可以正确读取。
     */
    private byte[] buildExcelBytes(List<CompoundTargetRel> data) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue(excelName("inchikey"));
        header.createCell(1).setCellValue(excelName("tcmTarId"));
        header.createCell(2).setCellValue(excelName("source"));

        int rowIdx = 1;
        for (CompoundTargetRel item : data) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getInchikey());
            row.createCell(1).setCellValue(item.getTcmTarId());
            row.createCell(2).setCellValue(item.getSource() == null ? "" : item.getSource());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    private String excelName(String fieldName) throws NoSuchFieldException {
        return CompoundTargetRel.class.getDeclaredField(fieldName).getAnnotation(Excel.class).name();
    }
}
