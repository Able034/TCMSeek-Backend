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
import com.tcmseek.webmanage.domain.PrescriptionOtherHerbRel;
import com.tcmseek.webmanage.service.IPrescriptionOtherHerbRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 方剂-其他中药关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/fohrelwebmanage")
public class PrescriptionOtherHerbRelController extends BaseController
{
    @Autowired
    private IPrescriptionOtherHerbRelService prescriptionOtherHerbRelService;

    /**
     * 查询方剂-其他中药关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fohrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(PrescriptionOtherHerbRel prescriptionOtherHerbRel)
    {
        startPage();
        List<PrescriptionOtherHerbRel> list = prescriptionOtherHerbRelService.selectPrescriptionOtherHerbRelList(prescriptionOtherHerbRel);
        return getDataTable(list);
    }

    /**
     * 导出方剂-其他中药关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fohrelwebmanage:export')")
    @Log(title = "方剂-其他中药关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PrescriptionOtherHerbRel prescriptionOtherHerbRel)
    {
        List<PrescriptionOtherHerbRel> list = prescriptionOtherHerbRelService.selectPrescriptionOtherHerbRelList(prescriptionOtherHerbRel);
        ExcelUtil<PrescriptionOtherHerbRel> util = new ExcelUtil<PrescriptionOtherHerbRel>(PrescriptionOtherHerbRel.class);
        util.exportExcel(response, list, "方剂-其他中药关联数据");
    }

    /**
     * 获取方剂-其他中药关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fohrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(prescriptionOtherHerbRelService.selectPrescriptionOtherHerbRelById(id));
    }

    /**
     * 新增方剂-其他中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fohrelwebmanage:add')")
    @Log(title = "方剂-其他中药关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PrescriptionOtherHerbRel prescriptionOtherHerbRel)
    {
        return toAjax(prescriptionOtherHerbRelService.insertPrescriptionOtherHerbRel(prescriptionOtherHerbRel));
    }

    /**
     * 修改方剂-其他中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fohrelwebmanage:edit')")
    @Log(title = "方剂-其他中药关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PrescriptionOtherHerbRel prescriptionOtherHerbRel)
    {
        return toAjax(prescriptionOtherHerbRelService.updatePrescriptionOtherHerbRel(prescriptionOtherHerbRel));
    }

    /**
     * 删除方剂-其他中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fohrelwebmanage:remove')")
    @Log(title = "方剂-其他中药关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(prescriptionOtherHerbRelService.deletePrescriptionOtherHerbRelByIds(ids));
    }
}
