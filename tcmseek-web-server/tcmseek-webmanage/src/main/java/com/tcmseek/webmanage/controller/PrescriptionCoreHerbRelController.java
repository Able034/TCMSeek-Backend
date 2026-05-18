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
import com.tcmseek.webmanage.domain.PrescriptionCoreHerbRel;
import com.tcmseek.webmanage.service.IPrescriptionCoreHerbRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 方剂-核心中药关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/fhrelwebmanage")
public class PrescriptionCoreHerbRelController extends BaseController
{
    @Autowired
    private IPrescriptionCoreHerbRelService prescriptionCoreHerbRelService;

    /**
     * 查询方剂-核心中药关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fhrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(PrescriptionCoreHerbRel prescriptionCoreHerbRel)
    {
        startPage();
        List<PrescriptionCoreHerbRel> list = prescriptionCoreHerbRelService.selectPrescriptionCoreHerbRelList(prescriptionCoreHerbRel);
        return getDataTable(list);
    }

    /**
     * 导出方剂-核心中药关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fhrelwebmanage:export')")
    @Log(title = "方剂-核心中药关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PrescriptionCoreHerbRel prescriptionCoreHerbRel)
    {
        List<PrescriptionCoreHerbRel> list = prescriptionCoreHerbRelService.selectPrescriptionCoreHerbRelList(prescriptionCoreHerbRel);
        ExcelUtil<PrescriptionCoreHerbRel> util = new ExcelUtil<PrescriptionCoreHerbRel>(PrescriptionCoreHerbRel.class);
        util.exportExcel(response, list, "方剂-核心中药关联数据");
    }

    /**
     * 获取方剂-核心中药关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fhrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(prescriptionCoreHerbRelService.selectPrescriptionCoreHerbRelById(id));
    }

    /**
     * 新增方剂-核心中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fhrelwebmanage:add')")
    @Log(title = "方剂-核心中药关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PrescriptionCoreHerbRel prescriptionCoreHerbRel)
    {
        return toAjax(prescriptionCoreHerbRelService.insertPrescriptionCoreHerbRel(prescriptionCoreHerbRel));
    }

    /**
     * 修改方剂-核心中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fhrelwebmanage:edit')")
    @Log(title = "方剂-核心中药关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PrescriptionCoreHerbRel prescriptionCoreHerbRel)
    {
        return toAjax(prescriptionCoreHerbRelService.updatePrescriptionCoreHerbRel(prescriptionCoreHerbRel));
    }

    /**
     * 删除方剂-核心中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fhrelwebmanage:remove')")
    @Log(title = "方剂-核心中药关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(prescriptionCoreHerbRelService.deletePrescriptionCoreHerbRelByIds(ids));
    }
}
