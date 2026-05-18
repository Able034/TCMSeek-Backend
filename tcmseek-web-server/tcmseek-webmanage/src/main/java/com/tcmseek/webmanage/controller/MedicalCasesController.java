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
import com.tcmseek.webmanage.domain.MedicalCases;
import com.tcmseek.webmanage.service.IMedicalCasesService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 医案信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/tcmcaseswebmanage")
public class MedicalCasesController extends BaseController
{
    @Autowired
    private IMedicalCasesService medicalCasesService;

    /**
     * 查询医案信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmcaseswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalCases medicalCases)
    {
        startPage();
        List<MedicalCases> list = medicalCasesService.selectMedicalCasesList(medicalCases);
        return getDataTable(list);
    }

    /**
     * 导出医案信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmcaseswebmanage:export')")
    @Log(title = "医案信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalCases medicalCases)
    {
        List<MedicalCases> list = medicalCasesService.selectMedicalCasesList(medicalCases);
        ExcelUtil<MedicalCases> util = new ExcelUtil<MedicalCases>(MedicalCases.class);
        util.exportExcel(response, list, "医案信息数据");
    }

    /**
     * 获取医案信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmcaseswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalCasesService.selectMedicalCasesById(id));
    }

    /**
     * 新增医案信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmcaseswebmanage:add')")
    @Log(title = "医案信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalCases medicalCases)
    {
        return toAjax(medicalCasesService.insertMedicalCases(medicalCases));
    }

    /**
     * 修改医案信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmcaseswebmanage:edit')")
    @Log(title = "医案信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalCases medicalCases)
    {
        return toAjax(medicalCasesService.updateMedicalCases(medicalCases));
    }

    /**
     * 删除医案信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmcaseswebmanage:remove')")
    @Log(title = "医案信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalCasesService.deleteMedicalCasesByIds(ids));
    }
}
