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
import com.tcmseek.webmanage.domain.Pathways;
import com.tcmseek.webmanage.service.IPathwaysService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 通路信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/pathwayswebmanage")
public class PathwaysController extends BaseController
{
    @Autowired
    private IPathwaysService pathwaysService;

    /**
     * 查询通路信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:pathwayswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(Pathways pathways)
    {
        startPage();
        List<Pathways> list = pathwaysService.selectPathwaysList(pathways);
        return getDataTable(list);
    }

    /**
     * 导出通路信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:pathwayswebmanage:export')")
    @Log(title = "通路信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Pathways pathways)
    {
        List<Pathways> list = pathwaysService.selectPathwaysList(pathways);
        ExcelUtil<Pathways> util = new ExcelUtil<Pathways>(Pathways.class);
        util.exportExcel(response, list, "通路信息数据");
    }

    /**
     * 获取通路信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:pathwayswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(pathwaysService.selectPathwaysById(id));
    }

    /**
     * 新增通路信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:pathwayswebmanage:add')")
    @Log(title = "通路信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Pathways pathways)
    {
        return toAjax(pathwaysService.insertPathways(pathways));
    }

    /**
     * 修改通路信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:pathwayswebmanage:edit')")
    @Log(title = "通路信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Pathways pathways)
    {
        return toAjax(pathwaysService.updatePathways(pathways));
    }

    /**
     * 删除通路信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:pathwayswebmanage:remove')")
    @Log(title = "通路信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(pathwaysService.deletePathwaysByIds(ids));
    }
}
