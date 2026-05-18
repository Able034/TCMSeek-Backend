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
import com.tcmseek.webmanage.domain.MedicalCaseHerbRel;
import com.tcmseek.webmanage.service.IMedicalCaseHerbRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 医案-核心中药关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/yahrelwebmanage")
public class MedicalCaseHerbRelController extends BaseController
{
    @Autowired
    private IMedicalCaseHerbRelService medicalCaseHerbRelService;

    /**
     * 查询医案-核心中药关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:yahrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalCaseHerbRel medicalCaseHerbRel)
    {
        startPage();
        List<MedicalCaseHerbRel> list = medicalCaseHerbRelService.selectMedicalCaseHerbRelList(medicalCaseHerbRel);
        return getDataTable(list);
    }

    /**
     * 导出医案-核心中药关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:yahrelwebmanage:export')")
    @Log(title = "医案-核心中药关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalCaseHerbRel medicalCaseHerbRel)
    {
        List<MedicalCaseHerbRel> list = medicalCaseHerbRelService.selectMedicalCaseHerbRelList(medicalCaseHerbRel);
        ExcelUtil<MedicalCaseHerbRel> util = new ExcelUtil<MedicalCaseHerbRel>(MedicalCaseHerbRel.class);
        util.exportExcel(response, list, "医案-核心中药关联数据");
    }

    /**
     * 获取医案-核心中药关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:yahrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalCaseHerbRelService.selectMedicalCaseHerbRelById(id));
    }

    /**
     * 新增医案-核心中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:yahrelwebmanage:add')")
    @Log(title = "医案-核心中药关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalCaseHerbRel medicalCaseHerbRel)
    {
        return toAjax(medicalCaseHerbRelService.insertMedicalCaseHerbRel(medicalCaseHerbRel));
    }

    /**
     * 修改医案-核心中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:yahrelwebmanage:edit')")
    @Log(title = "医案-核心中药关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalCaseHerbRel medicalCaseHerbRel)
    {
        return toAjax(medicalCaseHerbRelService.updateMedicalCaseHerbRel(medicalCaseHerbRel));
    }

    /**
     * 删除医案-核心中药关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:yahrelwebmanage:remove')")
    @Log(title = "医案-核心中药关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalCaseHerbRelService.deleteMedicalCaseHerbRelByIds(ids));
    }
}
