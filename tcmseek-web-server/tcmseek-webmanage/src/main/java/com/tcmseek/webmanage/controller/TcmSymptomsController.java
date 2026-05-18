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
import com.tcmseek.webmanage.domain.TcmSymptoms;
import com.tcmseek.webmanage.service.ITcmSymptomsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中医症状信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/tcmsymptomswebmanage")
public class TcmSymptomsController extends BaseController
{
    @Autowired
    private ITcmSymptomsService tcmSymptomsService;

    /**
     * 查询中医症状信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsymptomswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(TcmSymptoms tcmSymptoms)
    {
        startPage();
        List<TcmSymptoms> list = tcmSymptomsService.selectTcmSymptomsList(tcmSymptoms);
        return getDataTable(list);
    }

    /**
     * 导出中医症状信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsymptomswebmanage:export')")
    @Log(title = "中医症状信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TcmSymptoms tcmSymptoms)
    {
        List<TcmSymptoms> list = tcmSymptomsService.selectTcmSymptomsList(tcmSymptoms);
        ExcelUtil<TcmSymptoms> util = new ExcelUtil<TcmSymptoms>(TcmSymptoms.class);
        util.exportExcel(response, list, "中医症状信息数据");
    }

    /**
     * 获取中医症状信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsymptomswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tcmSymptomsService.selectTcmSymptomsById(id));
    }

    /**
     * 新增中医症状信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsymptomswebmanage:add')")
    @Log(title = "中医症状信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TcmSymptoms tcmSymptoms)
    {
        return toAjax(tcmSymptomsService.insertTcmSymptoms(tcmSymptoms));
    }

    /**
     * 修改中医症状信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsymptomswebmanage:edit')")
    @Log(title = "中医症状信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TcmSymptoms tcmSymptoms)
    {
        return toAjax(tcmSymptomsService.updateTcmSymptoms(tcmSymptoms));
    }

    /**
     * 删除中医症状信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsymptomswebmanage:remove')")
    @Log(title = "中医症状信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tcmSymptomsService.deleteTcmSymptomsByIds(ids));
    }
}
