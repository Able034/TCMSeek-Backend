package com.tcmseek.webmanage.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tcmseek.webmanage.mapper.TcmPrescriptionsMapper;
import com.tcmseek.webmanage.domain.TcmPrescriptions;
import com.tcmseek.webmanage.service.ITcmPrescriptionsService;

/**
 * TCM方剂（含中成药）信息Service业务层处理
 * 
 * @author Able
 * @date 2025-11-13
 */
@Service
public class TcmPrescriptionsServiceImpl implements ITcmPrescriptionsService 
{
    @Autowired
    private TcmPrescriptionsMapper tcmPrescriptionsMapper;

    /**
     * 查询TCM方剂（含中成药）信息
     * 
     * @param id TCM方剂（含中成药）信息主键
     * @return TCM方剂（含中成药）信息
     */
    @Override
    public TcmPrescriptions selectTcmPrescriptionsById(Long id)
    {
        return tcmPrescriptionsMapper.selectTcmPrescriptionsById(id);
    }

    /**
     * 查询TCM方剂（含中成药）信息列表
     * 
     * @param tcmPrescriptions TCM方剂（含中成药）信息
     * @return TCM方剂（含中成药）信息
     */
    @Override
    public List<TcmPrescriptions> selectTcmPrescriptionsList(TcmPrescriptions tcmPrescriptions)
    {
        return tcmPrescriptionsMapper.selectTcmPrescriptionsList(tcmPrescriptions);
    }

    /**
     * 新增TCM方剂（含中成药）信息
     * 
     * @param tcmPrescriptions TCM方剂（含中成药）信息
     * @return 结果
     */
    @Override
    public int insertTcmPrescriptions(TcmPrescriptions tcmPrescriptions)
    {
        return tcmPrescriptionsMapper.insertTcmPrescriptions(tcmPrescriptions);
    }

    /**
     * 修改TCM方剂（含中成药）信息
     * 
     * @param tcmPrescriptions TCM方剂（含中成药）信息
     * @return 结果
     */
    @Override
    public int updateTcmPrescriptions(TcmPrescriptions tcmPrescriptions)
    {
        return tcmPrescriptionsMapper.updateTcmPrescriptions(tcmPrescriptions);
    }

    /**
     * 批量删除TCM方剂（含中成药）信息
     * 
     * @param ids 需要删除的TCM方剂（含中成药）信息主键
     * @return 结果
     */
    @Override
    public int deleteTcmPrescriptionsByIds(Long[] ids)
    {
        return tcmPrescriptionsMapper.deleteTcmPrescriptionsByIds(ids);
    }

    /**
     * 删除TCM方剂（含中成药）信息信息
     * 
     * @param id TCM方剂（含中成药）信息主键
     * @return 结果
     */
    @Override
    public int deleteTcmPrescriptionsById(Long id)
    {
        return tcmPrescriptionsMapper.deleteTcmPrescriptionsById(id);
    }
}
