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
import com.tcmseek.webmanage.domain.PathwayTargetRel;
import com.tcmseek.webmanage.service.IPathwayTargetRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 通路-靶标关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/tlbbrelwebmanage")
public class PathwayTargetRelController extends BaseController
{
    @Autowired
    private IPathwayTargetRelService pathwayTargetRelService;

    /**
     * 查询通路-靶标关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tlbbrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(PathwayTargetRel pathwayTargetRel)
    {
        startPage();
        List<PathwayTargetRel> list = pathwayTargetRelService.selectPathwayTargetRelList(pathwayTargetRel);
        return getDataTable(list);
    }

    /**
     * 导出通路-靶标关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tlbbrelwebmanage:export')")
    @Log(title = "通路-靶标关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PathwayTargetRel pathwayTargetRel)
    {
        List<PathwayTargetRel> list = pathwayTargetRelService.selectPathwayTargetRelList(pathwayTargetRel);
        ExcelUtil<PathwayTargetRel> util = new ExcelUtil<PathwayTargetRel>(PathwayTargetRel.class);
        util.exportExcel(response, list, "通路-靶标关联数据");
    }

    /**
     * 获取通路-靶标关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tlbbrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(pathwayTargetRelService.selectPathwayTargetRelById(id));
    }

    /**
     * 新增通路-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tlbbrelwebmanage:add')")
    @Log(title = "通路-靶标关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PathwayTargetRel pathwayTargetRel)
    {
        return toAjax(pathwayTargetRelService.insertPathwayTargetRel(pathwayTargetRel));
    }

    /**
     * 修改通路-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tlbbrelwebmanage:edit')")
    @Log(title = "通路-靶标关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PathwayTargetRel pathwayTargetRel)
    {
        return toAjax(pathwayTargetRelService.updatePathwayTargetRel(pathwayTargetRel));
    }

    /**
     * 删除通路-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tlbbrelwebmanage:remove')")
    @Log(title = "通路-靶标关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(pathwayTargetRelService.deletePathwayTargetRelByIds(ids));
    }
}
