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
import com.tcmseek.webmanage.domain.WmSymptoms;
import com.tcmseek.webmanage.service.IWmSymptomsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 西医症状信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/symptomswebmanage")
public class WmSymptomsController extends BaseController
{
    @Autowired
    private IWmSymptomsService wmSymptomsService;

    /**
     * 查询西医症状信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:symptomswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmSymptoms wmSymptoms)
    {
        startPage();
        List<WmSymptoms> list = wmSymptomsService.selectWmSymptomsList(wmSymptoms);
        return getDataTable(list);
    }

    /**
     * 导出西医症状信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:symptomswebmanage:export')")
    @Log(title = "西医症状信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmSymptoms wmSymptoms)
    {
        List<WmSymptoms> list = wmSymptomsService.selectWmSymptomsList(wmSymptoms);
        ExcelUtil<WmSymptoms> util = new ExcelUtil<WmSymptoms>(WmSymptoms.class);
        util.exportExcel(response, list, "西医症状信息数据");
    }

    /**
     * 获取西医症状信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:symptomswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(wmSymptomsService.selectWmSymptomsById(id));
    }

    /**
     * 新增西医症状信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:symptomswebmanage:add')")
    @Log(title = "西医症状信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WmSymptoms wmSymptoms)
    {
        return toAjax(wmSymptomsService.insertWmSymptoms(wmSymptoms));
    }

    /**
     * 修改西医症状信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:symptomswebmanage:edit')")
    @Log(title = "西医症状信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmSymptoms wmSymptoms)
    {
        return toAjax(wmSymptomsService.updateWmSymptoms(wmSymptoms));
    }

    /**
     * 删除西医症状信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:symptomswebmanage:remove')")
    @Log(title = "西医症状信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(wmSymptomsService.deleteWmSymptomsByIds(ids));
    }
}
