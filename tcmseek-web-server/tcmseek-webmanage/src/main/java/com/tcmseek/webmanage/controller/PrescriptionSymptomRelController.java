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
import com.tcmseek.webmanage.domain.PrescriptionSymptomRel;
import com.tcmseek.webmanage.service.IPrescriptionSymptomRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 方剂-中医症状关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/fjtcmzzrel")
public class PrescriptionSymptomRelController extends BaseController
{
    @Autowired
    private IPrescriptionSymptomRelService prescriptionSymptomRelService;

    /**
     * 查询方剂-中医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjtcmzzrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(PrescriptionSymptomRel prescriptionSymptomRel)
    {
        startPage();
        List<PrescriptionSymptomRel> list = prescriptionSymptomRelService.selectPrescriptionSymptomRelList(prescriptionSymptomRel);
        return getDataTable(list);
    }

    /**
     * 导出方剂-中医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjtcmzzrel:export')")
    @Log(title = "方剂-中医症状关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PrescriptionSymptomRel prescriptionSymptomRel)
    {
        List<PrescriptionSymptomRel> list = prescriptionSymptomRelService.selectPrescriptionSymptomRelList(prescriptionSymptomRel);
        ExcelUtil<PrescriptionSymptomRel> util = new ExcelUtil<PrescriptionSymptomRel>(PrescriptionSymptomRel.class);
        util.exportExcel(response, list, "方剂-中医症状关联数据");
    }

    /**
     * 获取方剂-中医症状关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjtcmzzrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(prescriptionSymptomRelService.selectPrescriptionSymptomRelById(id));
    }

    /**
     * 新增方剂-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjtcmzzrel:add')")
    @Log(title = "方剂-中医症状关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PrescriptionSymptomRel prescriptionSymptomRel)
    {
        return toAjax(prescriptionSymptomRelService.insertPrescriptionSymptomRel(prescriptionSymptomRel));
    }

    /**
     * 修改方剂-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjtcmzzrel:edit')")
    @Log(title = "方剂-中医症状关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PrescriptionSymptomRel prescriptionSymptomRel)
    {
        return toAjax(prescriptionSymptomRelService.updatePrescriptionSymptomRel(prescriptionSymptomRel));
    }

    /**
     * 删除方剂-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:fjtcmzzrel:remove')")
    @Log(title = "方剂-中医症状关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(prescriptionSymptomRelService.deletePrescriptionSymptomRelByIds(ids));
    }
}
