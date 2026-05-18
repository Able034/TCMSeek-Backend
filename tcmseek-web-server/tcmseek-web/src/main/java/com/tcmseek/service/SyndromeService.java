package com.tcmseek.service;

import com.tcmseek.pojo.entity.TcmSymptom;
import com.tcmseek.pojo.entity.TcmSyndrome;
import com.tcmseek.pojo.entity.coreTcmHerbs;

import java.util.List;

/**
 * 证候信息Service接口
 */
public interface SyndromeService {

    List<TcmSyndrome> selectSyndromeList(String keyword, String category);

    TcmSyndrome selectSyndromeById(String tcmSyndromeId);

    List<TcmSymptom> selectSymptomsBySyndromeId(String tcmSyndromeId);

    List<coreTcmHerbs> selectHerbsBySyndromeId(String tcmSyndromeId);

    List<com.tcmseek.pojo.entity.TcmPrescription> selectPrescriptionsBySyndromeId(String tcmSyndromeId);
}

