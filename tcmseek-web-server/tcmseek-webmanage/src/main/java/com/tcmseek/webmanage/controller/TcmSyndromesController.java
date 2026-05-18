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
import com.tcmseek.webmanage.domain.TcmSyndromes;
import com.tcmseek.webmanage.service.ITcmSyndromesService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中医证候信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/tcmsyndromeswebmanage")
public class TcmSyndromesController extends BaseController
{
    @Autowired
    private ITcmSyndromesService tcmSyndromesService;

    /**
     * 查询中医证候信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsyndromeswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(TcmSyndromes tcmSyndromes)
    {
        startPage();
        List<TcmSyndromes> list = tcmSyndromesService.selectTcmSyndromesList(tcmSyndromes);
        return getDataTable(list);
    }

    /**
     * 导出中医证候信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsyndromeswebmanage:export')")
    @Log(title = "中医证候信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TcmSyndromes tcmSyndromes)
    {
        List<TcmSyndromes> list = tcmSyndromesService.selectTcmSyndromesList(tcmSyndromes);
        ExcelUtil<TcmSyndromes> util = new ExcelUtil<TcmSyndromes>(TcmSyndromes.class);
        util.exportExcel(response, list, "中医证候信息数据");
    }

    /**
     * 获取中医证候信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsyndromeswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tcmSyndromesService.selectTcmSyndromesById(id));
    }

    /**
     * 新增中医证候信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsyndromeswebmanage:add')")
    @Log(title = "中医证候信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TcmSyndromes tcmSyndromes)
    {
        return toAjax(tcmSyndromesService.insertTcmSyndromes(tcmSyndromes));
    }

    /**
     * 修改中医证候信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsyndromeswebmanage:edit')")
    @Log(title = "中医证候信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TcmSyndromes tcmSyndromes)
    {
        return toAjax(tcmSyndromesService.updateTcmSyndromes(tcmSyndromes));
    }

    /**
     * 删除中医证候信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:tcmsyndromeswebmanage:remove')")
    @Log(title = "中医证候信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tcmSyndromesService.deleteTcmSyndromesByIds(ids));
    }
}
