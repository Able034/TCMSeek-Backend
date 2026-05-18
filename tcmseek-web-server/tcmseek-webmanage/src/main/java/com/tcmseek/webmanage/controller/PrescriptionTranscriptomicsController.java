package com.tcmseek.webmanage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.tcmseek.webmanage.domain.PrescriptionTranscriptomics;
import com.tcmseek.webmanage.service.IPrescriptionTranscriptomicsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 方剂转录组学数据Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/ftrelwebmanage")
public class PrescriptionTranscriptomicsController extends BaseController
{
    @Autowired
    private IPrescriptionTranscriptomicsService prescriptionTranscriptomicsService;

    /**
     * 查询方剂转录组学数据列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:ftrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(PrescriptionTranscriptomics prescriptionTranscriptomics)
    {
        startPage();
        List<PrescriptionTranscriptomics> list = prescriptionTranscriptomicsService.selectPrescriptionTranscriptomicsList(prescriptionTranscriptomics);
        return getDataTable(list);
    }

    /**
     * 导出方剂转录组学数据列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:ftrelwebmanage:export')")
    @Log(title = "方剂转录组学数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PrescriptionTranscriptomics prescriptionTranscriptomics)
    {
        List<PrescriptionTranscriptomics> list = prescriptionTranscriptomicsService.selectPrescriptionTranscriptomicsList(prescriptionTranscriptomics);
        ExcelUtil<PrescriptionTranscriptomics> util = new ExcelUtil<PrescriptionTranscriptomics>(PrescriptionTranscriptomics.class);
        util.exportExcel(response, list, "方剂转录组学数据数据");
    }

    /**
     * 获取方剂转录组学数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:ftrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(prescriptionTranscriptomicsService.selectPrescriptionTranscriptomicsById(id));
    }

    /**
     * 新增方剂转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:ftrelwebmanage:add')")
    @Log(title = "方剂转录组学数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PrescriptionTranscriptomics prescriptionTranscriptomics)
    {
        return toAjax(prescriptionTranscriptomicsService.insertPrescriptionTranscriptomics(prescriptionTranscriptomics));
    }

    /**
     * 修改方剂转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:ftrelwebmanage:edit')")
    @Log(title = "方剂转录组学数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PrescriptionTranscriptomics prescriptionTranscriptomics)
    {
        return toAjax(prescriptionTranscriptomicsService.updatePrescriptionTranscriptomics(prescriptionTranscriptomics));
    }

    /**
     * 删除方剂转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:ftrelwebmanage:remove')")
    @Log(title = "方剂转录组学数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(prescriptionTranscriptomicsService.deletePrescriptionTranscriptomicsByIds(ids));
    }
}
