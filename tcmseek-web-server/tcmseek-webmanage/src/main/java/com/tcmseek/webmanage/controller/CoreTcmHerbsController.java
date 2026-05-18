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
import com.tcmseek.webmanage.domain.CoreTcmHerbs;
import com.tcmseek.webmanage.service.ICoreTcmHerbsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 核心中药信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/herbswebmanage")
public class CoreTcmHerbsController extends BaseController
{
    @Autowired
    private ICoreTcmHerbsService coreTcmHerbsService;

    /**
     * 查询核心中药信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:herbswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(CoreTcmHerbs coreTcmHerbs)
    {
        startPage();
        List<CoreTcmHerbs> list = coreTcmHerbsService.selectCoreTcmHerbsList(coreTcmHerbs);
        return getDataTable(list);
    }

    /**
     * 导出核心中药信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:herbswebmanage:export')")
    @Log(title = "核心中药信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CoreTcmHerbs coreTcmHerbs)
    {
        List<CoreTcmHerbs> list = coreTcmHerbsService.selectCoreTcmHerbsList(coreTcmHerbs);
        ExcelUtil<CoreTcmHerbs> util = new ExcelUtil<CoreTcmHerbs>(CoreTcmHerbs.class);
        util.exportExcel(response, list, "核心中药信息数据");
    }

    /**
     * 获取核心中药信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:herbswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(coreTcmHerbsService.selectCoreTcmHerbsById(id));
    }

    /**
     * 新增核心中药信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:herbswebmanage:add')")
    @Log(title = "核心中药信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CoreTcmHerbs coreTcmHerbs)
    {
        return toAjax(coreTcmHerbsService.insertCoreTcmHerbs(coreTcmHerbs));
    }

    /**
     * 修改核心中药信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:herbswebmanage:edit')")
    @Log(title = "核心中药信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CoreTcmHerbs coreTcmHerbs)
    {
        return toAjax(coreTcmHerbsService.updateCoreTcmHerbs(coreTcmHerbs));
    }

    /**
     * 删除核心中药信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:herbswebmanage:remove')")
    @Log(title = "核心中药信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(coreTcmHerbsService.deleteCoreTcmHerbsByIds(ids));
    }
}
