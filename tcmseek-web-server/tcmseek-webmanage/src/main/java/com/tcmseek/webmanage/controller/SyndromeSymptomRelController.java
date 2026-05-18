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
import com.tcmseek.webmanage.domain.SyndromeSymptomRel;
import com.tcmseek.webmanage.service.ISyndromeSymptomRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 证候-中医症状关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/zhtcmzzrel")
public class SyndromeSymptomRelController extends BaseController
{
    @Autowired
    private ISyndromeSymptomRelService syndromeSymptomRelService;

    /**
     * 查询证候-中医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhtcmzzrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(SyndromeSymptomRel syndromeSymptomRel)
    {
        startPage();
        List<SyndromeSymptomRel> list = syndromeSymptomRelService.selectSyndromeSymptomRelList(syndromeSymptomRel);
        return getDataTable(list);
    }

    /**
     * 导出证候-中医症状关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhtcmzzrel:export')")
    @Log(title = "证候-中医症状关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SyndromeSymptomRel syndromeSymptomRel)
    {
        List<SyndromeSymptomRel> list = syndromeSymptomRelService.selectSyndromeSymptomRelList(syndromeSymptomRel);
        ExcelUtil<SyndromeSymptomRel> util = new ExcelUtil<SyndromeSymptomRel>(SyndromeSymptomRel.class);
        util.exportExcel(response, list, "证候-中医症状关联数据");
    }

    /**
     * 获取证候-中医症状关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhtcmzzrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(syndromeSymptomRelService.selectSyndromeSymptomRelById(id));
    }

    /**
     * 新增证候-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhtcmzzrel:add')")
    @Log(title = "证候-中医症状关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SyndromeSymptomRel syndromeSymptomRel)
    {
        return toAjax(syndromeSymptomRelService.insertSyndromeSymptomRel(syndromeSymptomRel));
    }

    /**
     * 修改证候-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhtcmzzrel:edit')")
    @Log(title = "证候-中医症状关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SyndromeSymptomRel syndromeSymptomRel)
    {
        return toAjax(syndromeSymptomRelService.updateSyndromeSymptomRel(syndromeSymptomRel));
    }

    /**
     * 删除证候-中医症状关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:zhtcmzzrel:remove')")
    @Log(title = "证候-中医症状关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(syndromeSymptomRelService.deleteSyndromeSymptomRelByIds(ids));
    }
}
