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
import com.tcmseek.webmanage.domain.HerbDiseaseRel;
import com.tcmseek.webmanage.service.IHerbDiseaseRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中药-疾病关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/hdrel")
public class HerbDiseaseRelController extends BaseController
{
    @Autowired
    private IHerbDiseaseRelService herbDiseaseRelService;

    /**
     * 查询中药-疾病关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hdrel:list')")
    @GetMapping("/list")
    public TableDataInfo list(HerbDiseaseRel herbDiseaseRel)
    {
        startPage();
        List<HerbDiseaseRel> list = herbDiseaseRelService.selectHerbDiseaseRelList(herbDiseaseRel);
        return getDataTable(list);
    }

    /**
     * 导出中药-疾病关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hdrel:export')")
    @Log(title = "中药-疾病关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HerbDiseaseRel herbDiseaseRel)
    {
        List<HerbDiseaseRel> list = herbDiseaseRelService.selectHerbDiseaseRelList(herbDiseaseRel);
        ExcelUtil<HerbDiseaseRel> util = new ExcelUtil<HerbDiseaseRel>(HerbDiseaseRel.class);
        util.exportExcel(response, list, "中药-疾病关联数据");
    }

    /**
     * 获取中药-疾病关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hdrel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(herbDiseaseRelService.selectHerbDiseaseRelById(id));
    }

    /**
     * 新增中药-疾病关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hdrel:add')")
    @Log(title = "中药-疾病关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HerbDiseaseRel herbDiseaseRel)
    {
        return toAjax(herbDiseaseRelService.insertHerbDiseaseRel(herbDiseaseRel));
    }

    /**
     * 修改中药-疾病关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hdrel:edit')")
    @Log(title = "中药-疾病关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HerbDiseaseRel herbDiseaseRel)
    {
        return toAjax(herbDiseaseRelService.updateHerbDiseaseRel(herbDiseaseRel));
    }

    /**
     * 删除中药-疾病关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:hdrel:remove')")
    @Log(title = "中药-疾病关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(herbDiseaseRelService.deleteHerbDiseaseRelByIds(ids));
    }
}
