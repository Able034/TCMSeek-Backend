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
import com.tcmseek.webmanage.domain.Targets;
import com.tcmseek.webmanage.service.ITargetsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 靶标/基因信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/targetswebmanage")
public class TargetsController extends BaseController
{
    @Autowired
    private ITargetsService targetsService;

    /**
     * 查询靶标/基因信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:targetswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(Targets targets)
    {
        startPage();
        List<Targets> list = targetsService.selectTargetsList(targets);
        return getDataTable(list);
    }

    /**
     * 导出靶标/基因信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:targetswebmanage:export')")
    @Log(title = "靶标/基因信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Targets targets)
    {
        List<Targets> list = targetsService.selectTargetsList(targets);
        ExcelUtil<Targets> util = new ExcelUtil<Targets>(Targets.class);
        util.exportExcel(response, list, "靶标/基因信息数据");
    }

    /**
     * 获取靶标/基因信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:targetswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(targetsService.selectTargetsById(id));
    }

    /**
     * 新增靶标/基因信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:targetswebmanage:add')")
    @Log(title = "靶标/基因信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Targets targets)
    {
        return toAjax(targetsService.insertTargets(targets));
    }

    /**
     * 修改靶标/基因信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:targetswebmanage:edit')")
    @Log(title = "靶标/基因信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Targets targets)
    {
        return toAjax(targetsService.updateTargets(targets));
    }

    /**
     * 删除靶标/基因信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:targetswebmanage:remove')")
    @Log(title = "靶标/基因信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(targetsService.deleteTargetsByIds(ids));
    }
}
