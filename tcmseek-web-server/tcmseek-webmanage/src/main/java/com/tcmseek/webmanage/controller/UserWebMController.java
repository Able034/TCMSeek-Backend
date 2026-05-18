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
import com.tcmseek.webmanage.domain.UserWeb;
import com.tcmseek.webmanage.service.IUserWebMService;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.common.core.page.TableDataInfo;

/**
 * 用户信息Controller
 * 
 * @author able
 * @date 2025-11-13
 */
@RestController
@RequestMapping("/webmanage/userweb")
public class UserWebMController extends BaseController
{
    @Autowired
    private IUserWebMService userWebService;

    /**
     * 查询用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:userweb:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserWeb userWeb)
    {
        startPage();
        List<UserWeb> list = userWebService.selectUserWebList(userWeb);
        return getDataTable(list);
    }

    /**
     * 导出用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('webmanage:userweb:export')")
    @Log(title = "用户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserWeb userWeb)
    {
        List<UserWeb> list = userWebService.selectUserWebList(userWeb);
        ExcelUtil<UserWeb> util = new ExcelUtil<UserWeb>(UserWeb.class);
        util.exportExcel(response, list, "用户信息数据");
    }

    /**
     * 获取用户信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:userweb:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(userWebService.selectUserWebById(id));
    }

    /**
     * 新增用户信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:userweb:add')")
    @Log(title = "用户信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserWeb userWeb)
    {
        return toAjax(userWebService.insertUserWeb(userWeb));
    }

    /**
     * 修改用户信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:userweb:edit')")
    @Log(title = "用户信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserWeb userWeb)
    {
        return toAjax(userWebService.updateUserWeb(userWeb));
    }

    /**
     * 删除用户信息
     */
    @PreAuthorize("@ss.hasPermi('webmanage:userweb:remove')")
    @Log(title = "用户信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(userWebService.deleteUserWebByIds(ids));
    }
}
