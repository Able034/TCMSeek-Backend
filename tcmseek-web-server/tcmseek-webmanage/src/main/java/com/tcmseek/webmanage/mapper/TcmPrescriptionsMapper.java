package com.tcmseek.webmanage.mapper;

import java.util.List;
import com.tcmseek.webmanage.domain.TcmPrescriptions;

/**
 * TCM方剂（含中成药）信息Mapper接口
 * 
 * @author Able
 * @date 2025-11-13
 */
public interface TcmPrescriptionsMapper 
{
    /**
     * 查询TCM方剂（含中成药）信息
     * 
     * @param id TCM方剂（含中成药）信息主键
     * @return TCM方剂（含中成药）信息
     */
    public TcmPrescriptions selectTcmPrescriptionsById(Long id);

    /**
     * 查询TCM方剂（含中成药）信息列表
     * 
     * @param tcmPrescriptions TCM方剂（含中成药）信息
     * @return TCM方剂（含中成药）信息集合
     */
    public List<TcmPrescriptions> selectTcmPrescriptionsList(TcmPrescriptions tcmPrescriptions);

    /**
     * 新增TCM方剂（含中成药）信息
     * 
     * @param tcmPrescriptions TCM方剂（含中成药）信息
     * @return 结果
     */
    public int insertTcmPrescriptions(TcmPrescriptions tcmPrescriptions);

    /**
     * 修改TCM方剂（含中成药）信息
     * 
     * @param tcmPrescriptions TCM方剂（含中成药）信息
     * @return 结果
     */
    public int updateTcmPrescriptions(TcmPrescriptions tcmPrescriptions);

    /**
     * 删除TCM方剂（含中成药）信息
     * 
     * @param id TCM方剂（含中成药）信息主键
     * @return 结果
     */
    public int deleteTcmPrescriptionsById(Long id);

    /**
     * 批量删除TCM方剂（含中成药）信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTcmPrescriptionsByIds(Long[] ids);
}
