package com.tcmseek.webmanage.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.github.pagehelper.PageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tcmseek.common.annotation.Log;
import com.tcmseek.common.core.controller.BaseController;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.enums.BusinessType;
import com.tcmseek.webmanage.domain.Diseases;
import com.tcmseek.webmanage.service.IDiseasesService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 疾病信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/diseaseswebmanage")
public class DiseasesController extends BaseController
{
    @Autowired
    private IDiseasesService diseasesService;

    /**
     * 查询疾病信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:diseaseswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(Diseases diseases)
    {
        startPage();
        List<Diseases> list = diseasesService.selectDiseasesList(diseases);
        return getDataTable(list);
    }

    /**
     * 导出疾病信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:diseaseswebmanage:export')")
    @Log(title = "疾病信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Diseases diseases) throws IOException {
//        List<Diseases> list = diseasesService.selectDiseasesList(diseases);
//        ExcelUtil<Diseases> util = new ExcelUtil<Diseases>(Diseases.class);
//        util.exportExcel(response, list, "疾病信息数据");

//        ExcelWriter writer = EasyExcel.write(response.getOutputStream(), Diseases.class)
//                .autoCloseStream(false).build();
//        WriteSheet sheet = EasyExcel.writerSheet("疾病信息").build();
//        int pageNo = 1, pageSize = 2000;
//        List<Diseases> data;
//        do {
//            PageHelper.startPage(pageNo, pageSize, false);
//            data = diseasesService.selectDiseasesList(diseases);
//            if (data.isEmpty()) break;
//            writer.write(data, sheet);
//            pageNo++;
//        } while (data.size() == pageSize);
//        writer.finish();
        ExcelWriter writer = EasyExcel.write(response.getOutputStream(), Diseases.class)
                .autoCloseStream(false).build();
        WriteSheet sheet = EasyExcel.writerSheet("疾病信息")
                .excludeColumnFieldNames(java.util.Collections.singleton("params"))
                .build();
        int pageNo = 1, pageSize = 2000;
        List<Diseases> data;
        do {
            PageHelper.startPage(pageNo, pageSize, false);
            data = diseasesService.selectDiseasesList(diseases);
            if (data.isEmpty()) break;
            writer.write(data, sheet);
            pageNo++;
        } while (data.size() == pageSize);
        writer.finish();

    }

    /**
     * 获取疾病信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:diseaseswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(diseasesService.selectDiseasesById(id));
    }

    /**
     * 新增疾病信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:diseaseswebmanage:add')")
    @Log(title = "疾病信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Diseases diseases)
    {
        return toAjax(diseasesService.insertDiseases(diseases));
    }

    /**
     * 修改疾病信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:diseaseswebmanage:edit')")
    @Log(title = "疾病信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Diseases diseases)
    {
        return toAjax(diseasesService.updateDiseases(diseases));
    }

    /**
     * 删除疾病信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:diseaseswebmanage:remove')")
    @Log(title = "疾病信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(diseasesService.deleteDiseasesByIds(ids));
    }
}
