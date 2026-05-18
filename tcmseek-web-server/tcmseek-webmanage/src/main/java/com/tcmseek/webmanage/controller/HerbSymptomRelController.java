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
import com.tcmseek.webmanage.domain.HerbSymptomRel;
import com.tcmseek.webmanage.service.IHerbSymptomRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中药-中医症状关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/htcmzrel")
public class HerbSymptomRelController extends BaseController
{
    @Autowired
    private IHerbSymptomRelService herbSymptomRelService;

    /**
     * 查询中药-中医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htcmzrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(HerbSymptomRel herbSymptomRel)
    {
        startPage();
        List<HerbSymptomRel> list = herbSymptomRelService.selectHerbSymptomRelList(herbSymptomRel);
        return getDataTable(list);
    }

    /**
     * 导出中药-中医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htcmzrel:export')")
    @Log(title = "中药-中医症状关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HerbSymptomRel herbSymptomRel)
    {
        List<HerbSymptomRel> list = herbSymptomRelService.selectHerbSymptomRelList(herbSymptomRel);
        ExcelUtil<HerbSymptomRel> util = new ExcelUtil<HerbSymptomRel>(HerbSymptomRel.class);
        util.exportExcel(response, list, "中药-中医症状关联数据");
    }

    /**
     * 获取中药-中医症状关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htcmzrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(herbSymptomRelService.selectHerbSymptomRelById(id));
    }

    /**
     * 新增中药-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htcmzrel:add')")
    @Log(title = "中药-中医症状关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HerbSymptomRel herbSymptomRel)
    {
        return toAjax(herbSymptomRelService.insertHerbSymptomRel(herbSymptomRel));
    }

    /**
     * 修改中药-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htcmzrel:edit')")
    @Log(title = "中药-中医症状关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HerbSymptomRel herbSymptomRel)
    {
        return toAjax(herbSymptomRelService.updateHerbSymptomRel(herbSymptomRel));
    }

    /**
     * 删除中药-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htcmzrel:remove')")
    @Log(title = "中药-中医症状关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(herbSymptomRelService.deleteHerbSymptomRelByIds(ids));
    }
}
