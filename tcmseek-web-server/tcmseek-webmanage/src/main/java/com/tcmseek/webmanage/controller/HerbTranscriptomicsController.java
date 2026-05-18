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
import com.tcmseek.webmanage.domain.HerbTranscriptomics;
import com.tcmseek.webmanage.service.IHerbTranscriptomicsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中药材转录组学数据Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/htrelwebmanage")
public class HerbTranscriptomicsController extends BaseController
{
    @Autowired
    private IHerbTranscriptomicsService herbTranscriptomicsService;

    /**
     * 查询中药材转录组学数据列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(HerbTranscriptomics herbTranscriptomics)
    {
        startPage();
        List<HerbTranscriptomics> list = herbTranscriptomicsService.selectHerbTranscriptomicsList(herbTranscriptomics);
        return getDataTable(list);
    }

    /**
     * 导出中药材转录组学数据列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htrelwebmanage:export')")
    @Log(title = "中药材转录组学数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HerbTranscriptomics herbTranscriptomics)
    {
        List<HerbTranscriptomics> list = herbTranscriptomicsService.selectHerbTranscriptomicsList(herbTranscriptomics);
        ExcelUtil<HerbTranscriptomics> util = new ExcelUtil<HerbTranscriptomics>(HerbTranscriptomics.class);
        util.exportExcel(response, list, "中药材转录组学数据数据");
    }

    /**
     * 获取中药材转录组学数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(herbTranscriptomicsService.selectHerbTranscriptomicsById(id));
    }

    /**
     * 新增中药材转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htrelwebmanage:add')")
    @Log(title = "中药材转录组学数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HerbTranscriptomics herbTranscriptomics)
    {
        return toAjax(herbTranscriptomicsService.insertHerbTranscriptomics(herbTranscriptomics));
    }

    /**
     * 修改中药材转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htrelwebmanage:edit')")
    @Log(title = "中药材转录组学数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HerbTranscriptomics herbTranscriptomics)
    {
        return toAjax(herbTranscriptomicsService.updateHerbTranscriptomics(herbTranscriptomics));
    }

    /**
     * 删除中药材转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:htrelwebmanage:remove')")
    @Log(title = "中药材转录组学数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(herbTranscriptomicsService.deleteHerbTranscriptomicsByIds(ids));
    }
}
