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
import com.tcmseek.webmanage.domain.OtherTcmHerbs;
import com.tcmseek.webmanage.service.IOtherTcmHerbsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 其他中药信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/otherherbswebmanage")
public class OtherTcmHerbsController extends BaseController
{
    @Autowired
    private IOtherTcmHerbsService otherTcmHerbsService;

    /**
     * 查询其他中药信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:otherherbswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(OtherTcmHerbs otherTcmHerbs)
    {
        startPage();
        List<OtherTcmHerbs> list = otherTcmHerbsService.selectOtherTcmHerbsList(otherTcmHerbs);
        return getDataTable(list);
    }

    /**
     * 导出其他中药信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:otherherbswebmanage:export')")
    @Log(title = "其他中药信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OtherTcmHerbs otherTcmHerbs)
    {
        List<OtherTcmHerbs> list = otherTcmHerbsService.selectOtherTcmHerbsList(otherTcmHerbs);
        ExcelUtil<OtherTcmHerbs> util = new ExcelUtil<OtherTcmHerbs>(OtherTcmHerbs.class);
        util.exportExcel(response, list, "其他中药信息数据");
    }

    /**
     * 获取其他中药信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:otherherbswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(otherTcmHerbsService.selectOtherTcmHerbsById(id));
    }

    /**
     * 新增其他中药信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:otherherbswebmanage:add')")
    @Log(title = "其他中药信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OtherTcmHerbs otherTcmHerbs)
    {
        return toAjax(otherTcmHerbsService.insertOtherTcmHerbs(otherTcmHerbs));
    }

    /**
     * 修改其他中药信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:otherherbswebmanage:edit')")
    @Log(title = "其他中药信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OtherTcmHerbs otherTcmHerbs)
    {
        return toAjax(otherTcmHerbsService.updateOtherTcmHerbs(otherTcmHerbs));
    }

    /**
     * 删除其他中药信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:otherherbswebmanage:remove')")
    @Log(title = "其他中药信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(otherTcmHerbsService.deleteOtherTcmHerbsByIds(ids));
    }
}
