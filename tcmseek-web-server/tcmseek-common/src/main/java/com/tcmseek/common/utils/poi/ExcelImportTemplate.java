package com.tcmseek.common.utils.poi;

import com.tcmseek.common.exception.ServiceException;
import com.tcmseek.common.utils.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用的 Excel 导入模板工具。
 * 通过传入必填字段、唯一键字段以及查库/写库回调，完成去重、查重、分流插入/更新。
 */
public class ExcelImportTemplate {

    private static final int BATCH_SIZE = 500;
    private static final int QUERY_BATCH_SIZE = 800;

    public static class ImportResult {
        private final int total;// 总行数
        private final int valid;// 有效行数
        private final int inserted;// 插入行数
        private final int updated;// 更新行数
        private final int duplicatedInExcel;// Excel 内重复行数
        private final int missingRequired;// 缺少必填字段行数

        public ImportResult(int total, int valid, int inserted, int updated, int duplicatedInExcel, int missingRequired) {
            this.total = total;
            this.valid = valid;
            this.inserted = inserted;
            this.updated = updated;
            this.duplicatedInExcel = duplicatedInExcel;
            this.missingRequired = missingRequired;
        }

        public int getTotal() {
            return total;
        }

        public int getValid() {
            return valid;
        }

        public int getInserted() {
            return inserted;
        }

        public int getUpdated() {
            return updated;
        }

        public int getDuplicatedInExcel() {
            return duplicatedInExcel;
        }

        public int getMissingRequired() {
            return missingRequired;
        }
    }

    /**
     * 通用 Excel 导入。
     *
     * @param file         Excel 文件
     * @param excelEntity  Excel 行对应的实体类型
     * @param notNullFields 必填字段名列表
     * @param queryFields  唯一键字段名列表，用于 Excel 内去重及查库
     * @param findExists   批量查库函数，入参为唯一键列表，返回已存在的实体列表
     * @param batchInsert  批量插入函数
     * @param batchUpdate  批量更新函数
     */
    public static <T> ImportResult importExcel(MultipartFile file,
                                               Class<T> excelEntity,
                                               List<String> notNullFields,
                                               List<String> queryFields,
                                               Function<List<String>, List<T>> findExists,
                                               Consumer<List<T>> batchInsert,
                                               Consumer<List<T>> batchUpdate) {

        Objects.requireNonNull(excelEntity, "excelEntity must not be null");
        Objects.requireNonNull(notNullFields, "notNullFields must not be null");
        Objects.requireNonNull(queryFields, "queryFields must not be null");
        Objects.requireNonNull(findExists, "findExists must not be null");
        Objects.requireNonNull(batchInsert, "batchInsert must not be null");
        Objects.requireNonNull(batchUpdate, "batchUpdate must not be null");

        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件为空");
        }

        String fileName = file.getOriginalFilename();
        if (StringUtils.isNotEmpty(fileName)) {
            String lowerName = fileName.toLowerCase();
            if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")) {
                throw new ServiceException("仅支持xls 或xlsx 文件");
            }
        }

        // 解析 Excel
        List<T> importedList;
        try {
            ExcelUtil<T> util = new ExcelUtil<>(excelEntity);
            importedList = util.importEasyExcel(file.getInputStream());
        } catch (Exception e) {
            throw new ServiceException("解析 Excel 失败：" + e.getMessage()+e);
        }

        if (importedList == null || importedList.isEmpty()) {
            throw new ServiceException("Excel 中没有可导入的数据");
        }

        // Excel 内部去重、必填校验
        Map<String, T> excelUniqueMap = new LinkedHashMap<>();
        int duplicateInExcel = 0;
        int missingRequired = 0;

        for (T item : importedList) {
            if (!hasAllRequired(item, notNullFields)) {
                missingRequired++;
                continue;
            }
            String key = buildKey(item, queryFields, "|");
            if (StringUtils.isEmpty(key)) {
                missingRequired++;
                continue;
            }
            if (excelUniqueMap.containsKey(key)) {
                duplicateInExcel++;
                continue;
            }
            excelUniqueMap.put(key, item);
        }

        if (excelUniqueMap.isEmpty()) {
            throw new ServiceException("Excel 中没有有效可导入的数据");
        }

        // 批量查库
        Date now = new Date();
        List<String> allData = new ArrayList<>(excelUniqueMap.keySet());
        List<T> exists = new ArrayList<>();
        for (int i = 0; i < allData.size(); i += QUERY_BATCH_SIZE) {
            int end = Math.min(i + QUERY_BATCH_SIZE, allData.size());
            List<String> part = allData.subList(i, end);
            List<T> existPart = findExists.apply(part);
            if (existPart != null && !existPart.isEmpty()) {
                exists.addAll(existPart);
            }
        }

        Map<String, T> existsMap = new LinkedHashMap<>();
        for (T t : exists) {
            String key = buildKey(t, queryFields, "|");
            if (StringUtils.isNotEmpty(key) && !existsMap.containsKey(key)) {
                existsMap.put(key, t);
            }
        }

        List<T> insertList = new ArrayList<>();
        List<T> updateList = new ArrayList<>();
        for (Map.Entry<String, T> entry : excelUniqueMap.entrySet()) {
            String key = entry.getKey();
            T item = entry.getValue();
            if (existsMap.containsKey(key)) {
                T existed = existsMap.get(key);
                // 继承数据库已有的 id / createdAt，确保更新语句 where id 有值
                setFieldValue(item, "id", getFieldValue(existed, "id"));
                setFieldValue(item, "createdAt", getFieldValue(existed, "createdAt"));
                updateList.add(item);
            } else {
                if (getFieldValue(item, "createdAt") == null) {
                    setFieldValue(item, "createdAt", now);
                }
                insertList.add(item);
            }
        }

        // 批量插入
        for (int i = 0; i < insertList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, insertList.size());
            List<T> subList = insertList.subList(i, end);
            if (!subList.isEmpty()) {
                batchInsert.accept(subList);
            }
        }

        // 批量更新
        for (int i = 0; i < updateList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, updateList.size());
            List<T> subList = updateList.subList(i, end);
            if (!subList.isEmpty()) {
                batchUpdate.accept(subList);
            }
        }

        return new ImportResult(
                importedList.size(),
                excelUniqueMap.size(),
                insertList.size(),
                updateList.size(),
                duplicateInExcel,
                missingRequired
        );
    }

    /**
     * 获取字段值
     */
    private static Object getFieldValue(Object bean, String field) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(field, bean.getClass());
            Method getter = pd.getReadMethod();
            return getter.invoke(bean);
        } catch (Exception e) {
            throw new RuntimeException("读取字段失败: " + field, e);
        }
    }

    /**
     * 设置字段值（忽略不存在的 setter）
     */
    private static void setFieldValue(Object bean, String field, Object value) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(field, bean.getClass());
            Method setter = pd.getWriteMethod();
            if (setter != null) {
                setter.invoke(bean, value);
            }
        } catch (Exception ignored) {
            // 如果没有对应 setter，忽略
        }
    }

    /**
     * 判断对象对应字段属性是否非空
     */
    private static boolean hasAllRequired(Object bean, List<String> fields) {
        for (String f : fields) {
            Object v = getFieldValue(bean, f);
            if (v == null || StringUtils.isEmpty(v.toString().trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 构建对象唯一标识
     */
    private static String buildKey(Object bean, List<String> fields, String delimiter) {
        return fields.stream()
                .map(f -> {
                    Object v = getFieldValue(bean, f);
                    return v == null ? "" : v.toString().trim();
                })
                .collect(Collectors.joining(delimiter));
    }
}
