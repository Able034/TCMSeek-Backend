package com.tcmseek.webmanage.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.github.pagehelper.PageHelper;
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
import com.tcmseek.webmanage.domain.TcmCompounds;
import com.tcmseek.webmanage.service.ITcmCompoundsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 中药化合物理化性质Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/compoundswebmanage")
public class TcmCompoundsController extends BaseController
{
    @Autowired
    private ITcmCompoundsService tcmCompoundsService;

    /**
     * 查询中药化合物理化性质列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:compoundswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(TcmCompounds tcmCompounds)
    {
        startPage();
        List<TcmCompounds> list = tcmCompoundsService.selectTcmCompoundsList(tcmCompounds);
        return getDataTable(list);
    }

    /**
     * 导出中药化合物理化性质列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:compoundswebmanage:export')")
    @Log(title = "中药化合物理化性质", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TcmCompounds tcmCompounds) throws IOException {
//        List<TcmCompounds> list = tcmCompoundsService.selectTcmCompoundsList(tcmCompounds);
//        ExcelUtil<TcmCompounds> util = new ExcelUtil<TcmCompounds>(TcmCompounds.class);
//        util.exportExcel(response, list, "中药化合物理化性质数据");

        ExcelWriter writer = EasyExcel.write(response.getOutputStream(),TcmCompounds.class)
                .autoCloseStream(false).build();
        WriteSheet sheet = EasyExcel.writerSheet("中药化合物理化性质数据")
                .excludeColumnFieldNames(java.util.Collections.singleton("params"))
                .build();
        Long lastId = 0L;
        int pageSize = 2000;
        List<TcmCompounds> data;
        while(true){
            List<TcmCompounds> data1 = tcmCompoundsService.selectTcmCompoundsListexport(tcmCompounds,lastId,pageSize);
            if(data1.isEmpty()) break;
            writer.write(data1,sheet);
            lastId = data1.get(data1.size() -1).getId();
        }
        writer.finish();
    }

    /**
     * 获取中药化合物理化性质详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:compoundswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tcmCompoundsService.selectTcmCompoundsById(id));
    }

    /**
     * 新增中药化合物理化性质
     */
    @PreAuthorize("@ss.hasPermi('webmanage:compoundswebmanage:add')")
    @Log(title = "中药化合物理化性质", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TcmCompounds tcmCompounds)
    {
        return toAjax(tcmCompoundsService.insertTcmCompounds(tcmCompounds));
    }

    /**
     * 修改中药化合物理化性质
     */
    @PreAuthorize("@ss.hasPermi('webmanage:compoundswebmanage:edit')")
    @Log(title = "中药化合物理化性质", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TcmCompounds tcmCompounds)
    {
        return toAjax(tcmCompoundsService.updateTcmCompounds(tcmCompounds));
    }

    /**
     * 删除中药化合物理化性质
     */
    @PreAuthorize("@ss.hasPermi('webmanage:compoundswebmanage:remove')")
    @Log(title = "中药化合物理化性质", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tcmCompoundsService.deleteTcmCompoundsByIds(ids));
    }
}
