package com.tcmseek.dao;

import com.tcmseek.pojo.entity.TargetPredictionJob;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TargetPredictionJobMapper {

    int insert(TargetPredictionJob job);

    TargetPredictionJob selectById(@Param("id") Long id);

    List<TargetPredictionJob> selectByUserName(@Param("userName") String userName);

    int update(TargetPredictionJob job);
}
