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
import com.tcmseek.webmanage.domain.MedicalCasePrescriptionRel;
import com.tcmseek.webmanage.service.IMedicalCasePrescriptionRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 医案-方剂关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/tcmmfrel")
public class MedicalCasePrescriptionRelController extends BaseController
{
    @Autowired
    private IMedicalCasePrescriptionRelService medicalCasePrescriptionRelService;

    /**
     * 查询医案-方剂关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmmfrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(MedicalCasePrescriptionRel medicalCasePrescriptionRel)
    {
        startPage();
        List<MedicalCasePrescriptionRel> list = medicalCasePrescriptionRelService.selectMedicalCasePrescriptionRelList(medicalCasePrescriptionRel);
        return getDataTable(list);
    }

    /**
     * 导出医案-方剂关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmmfrel:export')")
    @Log(title = "医案-方剂关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MedicalCasePrescriptionRel medicalCasePrescriptionRel)
    {
        List<MedicalCasePrescriptionRel> list = medicalCasePrescriptionRelService.selectMedicalCasePrescriptionRelList(medicalCasePrescriptionRel);
        ExcelUtil<MedicalCasePrescriptionRel> util = new ExcelUtil<MedicalCasePrescriptionRel>(MedicalCasePrescriptionRel.class);
        util.exportExcel(response, list, "医案-方剂关联数据");
    }

    /**
     * 获取医案-方剂关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmmfrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(medicalCasePrescriptionRelService.selectMedicalCasePrescriptionRelById(id));
    }

    /**
     * 新增医案-方剂关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmmfrel:add')")
    @Log(title = "医案-方剂关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MedicalCasePrescriptionRel medicalCasePrescriptionRel)
    {
        return toAjax(medicalCasePrescriptionRelService.insertMedicalCasePrescriptionRel(medicalCasePrescriptionRel));
    }

    /**
     * 修改医案-方剂关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmmfrel:edit')")
    @Log(title = "医案-方剂关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MedicalCasePrescriptionRel medicalCasePrescriptionRel)
    {
        return toAjax(medicalCasePrescriptionRelService.updateMedicalCasePrescriptionRel(medicalCasePrescriptionRel));
    }

    /**
     * 删除医案-方剂关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmmfrel:remove')")
    @Log(title = "医案-方剂关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(medicalCasePrescriptionRelService.deleteMedicalCasePrescriptionRelByIds(ids));
    }
}
