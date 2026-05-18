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
import com.tcmseek.webmanage.domain.CompoundTargetRel;
import com.tcmseek.webmanage.service.ICompoundTargetRelService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

import static com.tcmseek.common.constant.Constants.Admin_Email;

/**
 * 化合物-靶标关联Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/cbbrelwebmanage")
public class CompoundTargetRelController extends BaseController
{
    @Autowired
    private ICompoundTargetRelService compoundTargetRelService;

    /**
     * 查询化合物-靶标关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cbbrelwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(CompoundTargetRel compoundTargetRel)
    {
        startPage();
        List<CompoundTargetRel> list = compoundTargetRelService.selectCompoundTargetRelList(compoundTargetRel);
        return getDataTable(list);
    }

    /***
     * excel导入
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cbbrelwebmanage:excelinput')")
    @PostMapping("/excelinput")
    public AjaxResult excelinput(MultipartFile file)
    {
        try{
            compoundTargetRelService.excelinput(file);
        }catch (Exception e){
            logger.error("服务端excel上传报错：{}", e);
            return error("上传excel错误，请联系管理员" + Admin_Email);
        }
        return success();
    }

    /**
     * 导出化合物-靶标关联列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cbbrelwebmanage:export')")
    @Log(title = "化合物-靶标关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompoundTargetRel compoundTargetRel)
    {
        List<CompoundTargetRel> list = compoundTargetRelService.selectCompoundTargetRelList(compoundTargetRel);
        ExcelUtil<CompoundTargetRel> util = new ExcelUtil<CompoundTargetRel>(CompoundTargetRel.class);
        util.exportExcel(response, list, "化合物-靶标关联数据");
    }

    /**
     * 获取化合物-靶标关联详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cbbrelwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(compoundTargetRelService.selectCompoundTargetRelById(id));
    }

    /**
     * 新增化合物-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cbbrelwebmanage:add')")
    @Log(title = "化合物-靶标关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompoundTargetRel compoundTargetRel)
    {
        return toAjax(compoundTargetRelService.insertCompoundTargetRel(compoundTargetRel));
    }

    /**
     * 修改化合物-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cbbrelwebmanage:edit')")
    @Log(title = "化合物-靶标关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompoundTargetRel compoundTargetRel)
    {
        return toAjax(compoundTargetRelService.updateCompoundTargetRel(compoundTargetRel));
    }

    /**
     * 删除化合物-靶标关联
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cbbrelwebmanage:remove')")
    @Log(title = "化合物-靶标关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(compoundTargetRelService.deleteCompoundTargetRelByIds(ids));
    }
}
