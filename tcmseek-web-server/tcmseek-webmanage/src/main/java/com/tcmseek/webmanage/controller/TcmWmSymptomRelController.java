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
import com.tcmseek.webmanage.domain.TcmWmSymptomRel;
import com.tcmseek.webmanage.service.ITcmWmSymptomRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中医症状-西医症状关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/tcmwmzzrel")
public class TcmWmSymptomRelController extends BaseController
{
    @Autowired
    private ITcmWmSymptomRelService tcmWmSymptomRelService;

    /**
     * 查询中医症状-西医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmwmzzrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(TcmWmSymptomRel tcmWmSymptomRel)
    {
        startPage();
        List<TcmWmSymptomRel> list = tcmWmSymptomRelService.selectTcmWmSymptomRelList(tcmWmSymptomRel);
        return getDataTable(list);
    }

    /**
     * 导出中医症状-西医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmwmzzrel:export')")
    @Log(title = "中医症状-西医症状关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TcmWmSymptomRel tcmWmSymptomRel)
    {
        List<TcmWmSymptomRel> list = tcmWmSymptomRelService.selectTcmWmSymptomRelList(tcmWmSymptomRel);
        ExcelUtil<TcmWmSymptomRel> util = new ExcelUtil<TcmWmSymptomRel>(TcmWmSymptomRel.class);
        util.exportExcel(response, list, "中医症状-西医症状关联数据");
    }

    /**
     * 获取中医症状-西医症状关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmwmzzrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tcmWmSymptomRelService.selectTcmWmSymptomRelById(id));
    }

    /**
     * 新增中医症状-西医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmwmzzrel:add')")
    @Log(title = "中医症状-西医症状关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TcmWmSymptomRel tcmWmSymptomRel)
    {
        return toAjax(tcmWmSymptomRelService.insertTcmWmSymptomRel(tcmWmSymptomRel));
    }

    /**
     * 修改中医症状-西医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmwmzzrel:edit')")
    @Log(title = "中医症状-西医症状关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TcmWmSymptomRel tcmWmSymptomRel)
    {
        return toAjax(tcmWmSymptomRelService.updateTcmWmSymptomRel(tcmWmSymptomRel));
    }

    /**
     * 删除中医症状-西医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmwmzzrel:remove')")
    @Log(title = "中医症状-西医症状关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tcmWmSymptomRelService.deleteTcmWmSymptomRelByIds(ids));
    }
}
