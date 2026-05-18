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
import com.tcmseek.webmanage.domain.TcmPrescriptions;
import com.tcmseek.webmanage.service.ITcmPrescriptionsService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * TCM方剂（含中成药）信息Controller
 * 
 * @author Able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/prescriptionswebmanage")
public class TcmPrescriptionsController extends BaseController
{
    @Autowired
    private ITcmPrescriptionsService tcmPrescriptionsService;

    /**
     * 查询TCM方剂（含中成药）信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:prescriptionswebmanage:list')")
    @GetMapping("/list")
    public TableDataInfo list(TcmPrescriptions tcmPrescriptions)
    {
        startPage();
        List<TcmPrescriptions> list = tcmPrescriptionsService.selectTcmPrescriptionsList(tcmPrescriptions);
        return getDataTable(list);
    }

    /**
     * 导出TCM方剂（含中成药）信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:prescriptionswebmanage:export')")
    @Log(title = "TCM方剂（含中成药）信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TcmPrescriptions tcmPrescriptions)
    {
        List<TcmPrescriptions> list = tcmPrescriptionsService.selectTcmPrescriptionsList(tcmPrescriptions);
        ExcelUtil<TcmPrescriptions> util = new ExcelUtil<TcmPrescriptions>(TcmPrescriptions.class);
        util.exportExcel(response, list, "TCM方剂（含中成药）信息数据");
    }

    /**
     * 获取TCM方剂（含中成药）信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:prescriptionswebmanage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tcmPrescriptionsService.selectTcmPrescriptionsById(id));
    }

    /**
     * 新增TCM方剂（含中成药）信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:prescriptionswebmanage:add')")
    @Log(title = "TCM方剂（含中成药）信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TcmPrescriptions tcmPrescriptions)
    {
        return toAjax(tcmPrescriptionsService.insertTcmPrescriptions(tcmPrescriptions));
    }

    /**
     * 修改TCM方剂（含中成药）信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:prescriptionswebmanage:edit')")
    @Log(title = "TCM方剂（含中成药）信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TcmPrescriptions tcmPrescriptions)
    {
        return toAjax(tcmPrescriptionsService.updateTcmPrescriptions(tcmPrescriptions));
    }

    /**
     * 删除TCM方剂（含中成药）信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:prescriptionswebmanage:remove')")
    @Log(title = "TCM方剂（含中成药）信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tcmPrescriptionsService.deleteTcmPrescriptionsByIds(ids));
    }
}
