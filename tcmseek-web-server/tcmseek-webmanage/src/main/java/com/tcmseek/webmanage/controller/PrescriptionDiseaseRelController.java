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
import com.tcmseek.webmanage.domain.PrescriptionDiseaseRel;
import com.tcmseek.webmanage.service.IPrescriptionDiseaseRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 方剂-疾病关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/fjjbrel")
public class PrescriptionDiseaseRelController extends BaseController
{
    @Autowired
    private IPrescriptionDiseaseRelService prescriptionDiseaseRelService;

    /**
     * 查询方剂-疾病关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjjbrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(PrescriptionDiseaseRel prescriptionDiseaseRel)
    {
        startPage();
        List<PrescriptionDiseaseRel> list = prescriptionDiseaseRelService.selectPrescriptionDiseaseRelList(prescriptionDiseaseRel);
        return getDataTable(list);
    }

    /**
     * 导出方剂-疾病关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjjbrel:export')")
    @Log(title = "方剂-疾病关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PrescriptionDiseaseRel prescriptionDiseaseRel)
    {
        List<PrescriptionDiseaseRel> list = prescriptionDiseaseRelService.selectPrescriptionDiseaseRelList(prescriptionDiseaseRel);
        ExcelUtil<PrescriptionDiseaseRel> util = new ExcelUtil<PrescriptionDiseaseRel>(PrescriptionDiseaseRel.class);
        util.exportExcel(response, list, "方剂-疾病关联数据");
    }

    /**
     * 获取方剂-疾病关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjjbrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(prescriptionDiseaseRelService.selectPrescriptionDiseaseRelById(id));
    }

    /**
     * 新增方剂-疾病关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjjbrel:add')")
    @Log(title = "方剂-疾病关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PrescriptionDiseaseRel prescriptionDiseaseRel)
    {
        return toAjax(prescriptionDiseaseRelService.insertPrescriptionDiseaseRel(prescriptionDiseaseRel));
    }

    /**
     * 修改方剂-疾病关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjjbrel:edit')")
    @Log(title = "方剂-疾病关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PrescriptionDiseaseRel prescriptionDiseaseRel)
    {
        return toAjax(prescriptionDiseaseRelService.updatePrescriptionDiseaseRel(prescriptionDiseaseRel));
    }

    /**
     * 删除方剂-疾病关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjjbrel:remove')")
    @Log(title = "方剂-疾病关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(prescriptionDiseaseRelService.deletePrescriptionDiseaseRelByIds(ids));
    }
}
