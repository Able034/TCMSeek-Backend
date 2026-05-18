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
import com.tcmseek.webmanage.domain.PhenotypeTargetRel;
import com.tcmseek.webmanage.service.IPhenotypeTargetRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 型-靶标关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/bxbbrelwebmanage")
public class PhenotypeTargetRelController extends BaseController
{
    @Autowired
    private IPhenotypeTargetRelService phenotypeTargetRelService;

    /**
     * 查询型-靶标关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:bxbbrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(PhenotypeTargetRel phenotypeTargetRel)
    {
        startPage();
        List<PhenotypeTargetRel> list = phenotypeTargetRelService.selectPhenotypeTargetRelList(phenotypeTargetRel);
        return getDataTable(list);
    }

    /**
     * 导出型-靶标关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:bxbbrelwebmanage:export')")
    @Log(title = "型-靶标关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PhenotypeTargetRel phenotypeTargetRel)
    {
        List<PhenotypeTargetRel> list = phenotypeTargetRelService.selectPhenotypeTargetRelList(phenotypeTargetRel);
        ExcelUtil<PhenotypeTargetRel> util = new ExcelUtil<PhenotypeTargetRel>(PhenotypeTargetRel.class);
        util.exportExcel(response, list, "型-靶标关联数据");
    }

    /**
     * 获取型-靶标关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:bxbbrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(phenotypeTargetRelService.selectPhenotypeTargetRelById(id));
    }

    /**
     * 新增型-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:bxbbrelwebmanage:add')")
    @Log(title = "型-靶标关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PhenotypeTargetRel phenotypeTargetRel)
    {
        return toAjax(phenotypeTargetRelService.insertPhenotypeTargetRel(phenotypeTargetRel));
    }

    /**
     * 修改型-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:bxbbrelwebmanage:edit')")
    @Log(title = "型-靶标关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PhenotypeTargetRel phenotypeTargetRel)
    {
        return toAjax(phenotypeTargetRelService.updatePhenotypeTargetRel(phenotypeTargetRel));
    }

    /**
     * 删除型-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:bxbbrelwebmanage:remove')")
    @Log(title = "型-靶标关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(phenotypeTargetRelService.deletePhenotypeTargetRelByIds(ids));
    }
}
