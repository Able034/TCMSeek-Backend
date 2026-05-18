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
import com.tcmseek.webmanage.domain.Phenotypes;
import com.tcmseek.webmanage.service.IPhenotypesService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 表型信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/phenotypeswebmanage")
public class PhenotypesController extends BaseController
{
    @Autowired
    private IPhenotypesService phenotypesService;

    /**
     * 查询表型信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:phenotypeswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(Phenotypes phenotypes)
    {
        startPage();
        List<Phenotypes> list = phenotypesService.selectPhenotypesList(phenotypes);
        return getDataTable(list);
    }

    /**
     * 导出表型信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:phenotypeswebmanage:export')")
    @Log(title = "表型信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Phenotypes phenotypes)
    {
        List<Phenotypes> list = phenotypesService.selectPhenotypesList(phenotypes);
        ExcelUtil<Phenotypes> util = new ExcelUtil<Phenotypes>(Phenotypes.class);
        util.exportExcel(response, list, "表型信息数据");
    }

    /**
     * 获取表型信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:phenotypeswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(phenotypesService.selectPhenotypesById(id));
    }

    /**
     * 新增表型信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:phenotypeswebmanage:add')")
    @Log(title = "表型信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Phenotypes phenotypes)
    {
        return toAjax(phenotypesService.insertPhenotypes(phenotypes));
    }

    /**
     * 修改表型信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:phenotypeswebmanage:edit')")
    @Log(title = "表型信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Phenotypes phenotypes)
    {
        return toAjax(phenotypesService.updatePhenotypes(phenotypes));
    }

    /**
     * 删除表型信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:phenotypeswebmanage:remove')")
    @Log(title = "表型信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(phenotypesService.deletePhenotypesByIds(ids));
    }
}
