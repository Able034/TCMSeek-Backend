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
import com.tcmseek.webmanage.domain.WmSymptomGeneRel;
import com.tcmseek.webmanage.service.IWmSymptomGeneRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 西医症状-基因关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/wxjyrel")
public class WmSymptomGeneRelController extends BaseController
{
    @Autowired
    private IWmSymptomGeneRelService wmSymptomGeneRelService;

    /**
     * 查询西医症状-基因关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:wxjyrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmSymptomGeneRel wmSymptomGeneRel)
    {
        startPage();
        List<WmSymptomGeneRel> list = wmSymptomGeneRelService.selectWmSymptomGeneRelList(wmSymptomGeneRel);
        return getDataTable(list);
    }

    /**
     * 导出西医症状-基因关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:wxjyrel:export')")
    @Log(title = "西医症状-基因关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmSymptomGeneRel wmSymptomGeneRel)
    {
        List<WmSymptomGeneRel> list = wmSymptomGeneRelService.selectWmSymptomGeneRelList(wmSymptomGeneRel);
        ExcelUtil<WmSymptomGeneRel> util = new ExcelUtil<WmSymptomGeneRel>(WmSymptomGeneRel.class);
        util.exportExcel(response, list, "西医症状-基因关联数据");
    }

    /**
     * 获取西医症状-基因关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:wxjyrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(wmSymptomGeneRelService.selectWmSymptomGeneRelById(id));
    }

    /**
     * 新增西医症状-基因关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:wxjyrel:add')")
    @Log(title = "西医症状-基因关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WmSymptomGeneRel wmSymptomGeneRel)
    {
        return toAjax(wmSymptomGeneRelService.insertWmSymptomGeneRel(wmSymptomGeneRel));
    }

    /**
     * 修改西医症状-基因关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:wxjyrel:edit')")
    @Log(title = "西医症状-基因关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmSymptomGeneRel wmSymptomGeneRel)
    {
        return toAjax(wmSymptomGeneRelService.updateWmSymptomGeneRel(wmSymptomGeneRel));
    }

    /**
     * 删除西医症状-基因关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:wxjyrel:remove')")
    @Log(title = "西医症状-基因关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(wmSymptomGeneRelService.deleteWmSymptomGeneRelByIds(ids));
    }
}
