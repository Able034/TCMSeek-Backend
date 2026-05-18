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
import com.tcmseek.webmanage.domain.HerbSyndromeRel;
import com.tcmseek.webmanage.service.IHerbSyndromeRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中药-证候关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/hzrelwebmanage")
public class HerbSyndromeRelController extends BaseController
{
    @Autowired
    private IHerbSyndromeRelService herbSyndromeRelService;

    /**
     * 查询中药-证候关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hzrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(HerbSyndromeRel herbSyndromeRel)
    {
        startPage();
        List<HerbSyndromeRel> list = herbSyndromeRelService.selectHerbSyndromeRelList(herbSyndromeRel);
        return getDataTable(list);
    }

    /**
     * 导出中药-证候关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hzrelwebmanage:export')")
    @Log(title = "中药-证候关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HerbSyndromeRel herbSyndromeRel)
    {
        List<HerbSyndromeRel> list = herbSyndromeRelService.selectHerbSyndromeRelList(herbSyndromeRel);
        ExcelUtil<HerbSyndromeRel> util = new ExcelUtil<HerbSyndromeRel>(HerbSyndromeRel.class);
        util.exportExcel(response, list, "中药-证候关联数据");
    }

    /**
     * 获取中药-证候关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hzrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(herbSyndromeRelService.selectHerbSyndromeRelById(id));
    }

    /**
     * 新增中药-证候关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hzrelwebmanage:add')")
    @Log(title = "中药-证候关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HerbSyndromeRel herbSyndromeRel)
    {
        return toAjax(herbSyndromeRelService.insertHerbSyndromeRel(herbSyndromeRel));
    }

    /**
     * 修改中药-证候关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hzrelwebmanage:edit')")
    @Log(title = "中药-证候关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HerbSyndromeRel herbSyndromeRel)
    {
        return toAjax(herbSyndromeRelService.updateHerbSyndromeRel(herbSyndromeRel));
    }

    /**
     * 删除中药-证候关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hzrelwebmanage:remove')")
    @Log(title = "中药-证候关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(herbSyndromeRelService.deleteHerbSyndromeRelByIds(ids));
    }
}
