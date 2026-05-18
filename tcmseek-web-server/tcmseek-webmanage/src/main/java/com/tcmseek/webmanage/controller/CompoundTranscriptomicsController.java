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
import com.tcmseek.webmanage.domain.CompoundTranscriptomics;
import com.tcmseek.webmanage.service.ICompoundTranscriptomicsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

import static com.tcmseek.common.constant.Constants.Admin_Email;

/**
 * 化合物转录组学数据Controller
 * 
 * @author Able
 * @date 2025-11-15
 */
@RestController
@RequestMapping("/webmanage/cTwebmanage")
public class CompoundTranscriptomicsController extends BaseController
{
    @Autowired
    private ICompoundTranscriptomicsService compoundTranscriptomicsService;

    /**
     * 查询化合物转录组学数据列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cTwebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(CompoundTranscriptomics compoundTranscriptomics)
    {
        startPage();
        List<CompoundTranscriptomics> list = compoundTranscriptomicsService.selectCompoundTranscriptomicsList(compoundTranscriptomics);
        return getDataTable(list);
    }

    /**
     * 导出化合物转录组学数据列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cTwebmanage:export')")
    @Log(title = "化合物转录组学数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompoundTranscriptomics compoundTranscriptomics)
    {
        List<CompoundTranscriptomics> list = compoundTranscriptomicsService.selectCompoundTranscriptomicsList(compoundTranscriptomics);
        ExcelUtil<CompoundTranscriptomics> util = new ExcelUtil<CompoundTranscriptomics>(CompoundTranscriptomics.class);
        util.exportExcel(response, list, "化合物转录组学数据数据");
    }

    /**
     *  excel导入数据
     * @param file
     * @return
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cTwebmanage:import')")
    @Log(title = "化合物转录组学数据", businessType = BusinessType.IMPORT)
    @PostMapping("/excelinput")
    public AjaxResult excelinput(MultipartFile file) {
        try{
            compoundTranscriptomicsService.excelinput(file);
        }catch (Exception e){
            return error("上传失败，请联系管理员"+ Admin_Email);
        }
        return success("上传成功");
    }

    /**
     * 获取化合物转录组学数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cTwebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(compoundTranscriptomicsService.selectCompoundTranscriptomicsById(id));
    }

    /**
     * 新增化合物转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cTwebmanage:add')")
    @Log(title = "化合物转录组学数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompoundTranscriptomics compoundTranscriptomics)
    {
        return toAjax(compoundTranscriptomicsService.insertCompoundTranscriptomics(compoundTranscriptomics));
    }

    /**
     * 修改化合物转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cTwebmanage:edit')")
    @Log(title = "化合物转录组学数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompoundTranscriptomics compoundTranscriptomics)
    {
        return toAjax(compoundTranscriptomicsService.updateCompoundTranscriptomics(compoundTranscriptomics));
    }

    /**
     * 删除化合物转录组学数据
     */
    @PreAuthorize("@ss.hasPermi('webmanage:cTwebmanage:remove')")
    @Log(title = "化合物转录组学数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(compoundTranscriptomicsService.deleteCompoundTranscriptomicsByIds(ids));
    }
}
