package com.tcmseek.webmanage.util;

import com.tcmseek.common.annotation.Excel;
import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.common.utils.poi.ExcelImportTemplate;
import com.tcmseek.common.utils.poi.ExcelImportTemplate.ImportResult;
import com.tcmseek.webmanage.domain.CompoundTargetRel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

class ExcelImportTemplateTest {

    @Test
    void importExcel_should_dedup_and_split_insert_update() throws Exception {
        List<CompoundTargetRel> excelData = new ArrayList<>();
        CompoundTargetRel first = new CompoundTargetRel();
        first.setInchikey("AAA");
        first.setTcmTarId("T1");
        first.setSource("S1");

        CompoundTargetRel second = new CompoundTargetRel();
        second.setInchikey("BBB");
        second.setTcmTarId("T2");
        second.setSource("S2");

        CompoundTargetRel duplicate = new CompoundTargetRel();
        duplicate.setInchikey("AAA");
        duplicate.setTcmTarId("T1");

        excelData.add(first);
        excelData.add(second);
        excelData.add(duplicate);

        MultipartFile file = new MockMultipartFile(
                "file",
                "rel.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildExcelBytes(excelData)
        );

        AtomicReference<List<String>> queriedKeys = new AtomicReference<>();
        List<List<CompoundTargetRel>> insertedBatches = new ArrayList<>();
        List<List<CompoundTargetRel>> updatedBatches = new ArrayList<>();

        Function<List<String>, List<CompoundTargetRel>> findExists = keys -> {
            queriedKeys.set(new ArrayList<>(keys));
            CompoundTargetRel existed = new CompoundTargetRel();
            existed.setInchikey("BBB");
            existed.setTcmTarId("T2");
            return Collections.singletonList(existed);
        };
        Consumer<List<CompoundTargetRel>> batchInsert = batch -> insertedBatches.add(new ArrayList<>(batch));
        Consumer<List<CompoundTargetRel>> batchUpdate = batch -> updatedBatches.add(new ArrayList<>(batch));

        ImportResult result = ExcelImportTemplate.importExcel(
                file,
                CompoundTargetRel.class,
                Arrays.asList("inchikey", "tcmTarId"),
                Arrays.asList("inchikey", "tcmTarId"),
                findExists,
                batchInsert,
                batchUpdate
        );

        Assertions.assertEquals(3, result.getTotal());
        Assertions.assertEquals(2, result.getValid());
        Assertions.assertEquals(1, result.getInserted());
        Assertions.assertEquals(1, result.getUpdated());
        Assertions.assertEquals(1, result.getDuplicatedInExcel());
        Assertions.assertEquals(0, result.getMissingRequired());

        Assertions.assertEquals(Arrays.asList("AAA|T1", "BBB|T2"), queriedKeys.get());
        Assertions.assertEquals(1, insertedBatches.size());
        Assertions.assertEquals("AAA", insertedBatches.get(0).get(0).getInchikey());
        Assertions.assertEquals(1, updatedBatches.size());
        Assertions.assertEquals("BBB", updatedBatches.get(0).get(0).getInchikey());
    }

    @Test
    void importExcel_should_throw_when_no_valid_rows() throws Exception {
        CompoundTargetRel invalid = new CompoundTargetRel();
        invalid.setInchikey("AAA");
        // tcmTarId 缺失 -> 不满足必填
        MultipartFile file = new MockMultipartFile(
                "file",
                "rel.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildExcelBytes(Collections.singletonList(invalid))
        );

        Assertions.assertThrows(ServiceException.class, () -> ExcelImportTemplate.importExcel(
                file,
                CompoundTargetRel.class,
                Arrays.asList("inchikey", "tcmTarId"),
                Arrays.asList("inchikey", "tcmTarId"),
                keys -> Collections.emptyList(),
                batch -> {},
                batch -> {}
        ));
    }

    private byte[] buildExcelBytes(List<CompoundTargetRel> data) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue(excelName("id"));
        header.createCell(1).setCellValue(excelName("inchikey"));
        header.createCell(2).setCellValue(excelName("tcmTarId"));
        header.createCell(3).setCellValue(excelName("source"));

        int rowIdx = 1;
        for (CompoundTargetRel item : data) {
            Row row = sheet.createRow(rowIdx++);
            // id 列可为空或递增，这里用行号避免 Long 转换异常
            row.createCell(0).setCellValue(rowIdx);
            row.createCell(1).setCellValue(item.getInchikey());
            row.createCell(2).setCellValue(item.getTcmTarId());
            row.createCell(3).setCellValue(item.getSource() == null ? "" : item.getSource());
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
