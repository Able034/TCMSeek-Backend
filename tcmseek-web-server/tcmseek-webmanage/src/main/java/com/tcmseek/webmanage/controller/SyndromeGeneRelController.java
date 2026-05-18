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
import com.tcmseek.webmanage.domain.SyndromeGeneRel;
import com.tcmseek.webmanage.service.ISyndromeGeneRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 证候-基因关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/zhjyrel")
public class SyndromeGeneRelController extends BaseController
{
    @Autowired
    private ISyndromeGeneRelService syndromeGeneRelService;

    /**
     * 查询证候-基因关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhjyrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(SyndromeGeneRel syndromeGeneRel)
    {
        startPage();
        List<SyndromeGeneRel> list = syndromeGeneRelService.selectSyndromeGeneRelList(syndromeGeneRel);
        return getDataTable(list);
    }

    /**
     * 导出证候-基因关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhjyrel:export')")
    @Log(title = "证候-基因关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SyndromeGeneRel syndromeGeneRel)
    {
        List<SyndromeGeneRel> list = syndromeGeneRelService.selectSyndromeGeneRelList(syndromeGeneRel);
        ExcelUtil<SyndromeGeneRel> util = new ExcelUtil<SyndromeGeneRel>(SyndromeGeneRel.class);
        util.exportExcel(response, list, "证候-基因关联数据");
    }

    /**
     * 获取证候-基因关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhjyrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(syndromeGeneRelService.selectSyndromeGeneRelById(id));
    }

    /**
     * 新增证候-基因关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhjyrel:add')")
    @Log(title = "证候-基因关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SyndromeGeneRel syndromeGeneRel)
    {
        return toAjax(syndromeGeneRelService.insertSyndromeGeneRel(syndromeGeneRel));
    }

    /**
     * 修改证候-基因关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhjyrel:edit')")
    @Log(title = "证候-基因关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SyndromeGeneRel syndromeGeneRel)
    {
        return toAjax(syndromeGeneRelService.updateSyndromeGeneRel(syndromeGeneRel));
    }

    /**
     * 删除证候-基因关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhjyrel:remove')")
    @Log(title = "证候-基因关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(syndromeGeneRelService.deleteSyndromeGeneRelByIds(ids));
    }
}
