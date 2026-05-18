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
import com.tcmseek.webmanage.domain.CompoundAdmet;
import com.tcmseek.webmanage.service.ICompoundAdmetService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

import static com.tcmseek.common.constant.Constants.Admin_Email;

/**
 * 化合物ADMET预测结果Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/admetwebmanage")
public class CompoundAdmetController extends BaseController
{
    @Autowired
    private ICompoundAdmetService compoundAdmetService;

    /**
     * 查询化合物ADMET预测结果列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:admetwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(CompoundAdmet compoundAdmet)
    {
        startPage();
        List<CompoundAdmet> list = compoundAdmetService.selectCompoundAdmetList(compoundAdmet);
        return getDataTable(list);
    }


    /**
     *excel导入化合物ADMET
     */
    @PreAuthorize("@ss.hasPermi('webmanage:admetwebmanage:excelinput')")
    @Log(title = "化合物ADMET预测结果", businessType = BusinessType.INSERT)
    @PostMapping("/excelinput")
    public AjaxResult excelinput(MultipartFile file){
        try{
            compoundAdmetService.excelinput(file);
        }catch (Exception e){
            logger.error("服务端excel上传报错：{}", e);
            return error("上传excel错误，请联系管理员" + Admin_Email);
        }

        return success();
    }

    /**
     * 导出化合物ADMET预测结果列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:admetwebmanage:export')")
    @Log(title = "化合物ADMET预测结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompoundAdmet compoundAdmet)
    {
        List<CompoundAdmet> list = compoundAdmetService.selectCompoundAdmetList(compoundAdmet);
        ExcelUtil<CompoundAdmet> util = new ExcelUtil<CompoundAdmet>(CompoundAdmet.class);
        util.exportExcel(response, list, "化合物ADMET预测结果数据");
    }

    /**
     * 获取化合物ADMET预测结果详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:admetwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(compoundAdmetService.selectCompoundAdmetById(id));
    }

    /**
     * 新增化合物ADMET预测结果
     */
    @PreAuthorize("@ss.hasPermi('webmanage:admetwebmanage:add')")
    @Log(title = "化合物ADMET预测结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompoundAdmet compoundAdmet)
    {
        return toAjax(compoundAdmetService.insertCompoundAdmet(compoundAdmet));
    }

    /**
     * 修改化合物ADMET预测结果
     */
    @PreAuthorize("@ss.hasPermi('webmanage:admetwebmanage:edit')")
    @Log(title = "化合物ADMET预测结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompoundAdmet compoundAdmet)
    {
        return toAjax(compoundAdmetService.updateCompoundAdmet(compoundAdmet));
    }

    /**
     * 删除化合物ADMET预测结果
     */
    @PreAuthorize("@ss.hasPermi('webmanage:admetwebmanage:remove')")
    @Log(title = "化合物ADMET预测结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(compoundAdmetService.deleteCompoundAdmetByIds(ids));
    }
}
