package com.tcmseek.mapper;

import com.tcmseek.dto.StructureSearchResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 化合物结构搜索 Mapper
 * Compound Structure Search Mapper
 */
@Mapper
public interface CompoundStructureMapper {
    
    /**
     * 全结构匹配搜索
     * @param molText MOL 文件内容
     * @return 匹配的化合物列表
     */
    @Select("SELECT * FROM search_exact_structure(#{molText})")
    List<StructureSearchResult> searchExactStructure(@Param("molText") String molText);
    
    /**
     * 子结构搜索
     * @param molText MOL 文件内容
     * @return 包含该子结构的化合物列表
     */
    @Select("SELECT * FROM search_substructure(#{molText})")
    List<StructureSearchResult> searchSubstructure(@Param("molText") String molText);
    
    /**
     * 相似性搜索
     * @param molText MOL 文件内容
     * @param threshold 相似度阈值 (0.1-1.0)
     * @return 相似的化合物列表（按相似度降序）
     */
    @Select("SELECT * FROM search_similarity(#{molText}, #{threshold})")
    List<StructureSearchResult> searchSimilarity(
        @Param("molText") String molText, 
        @Param("threshold") Double threshold
    );

    /**
     * Retrieve RDKit extension version to verify availability.
     */
    @Select("SELECT rdkit_version()")
    String getRDKitVersion();

    /**
     * Call a simple RDKit function to ensure cartridge works.
     */
    @Select("SELECT mol_to_smiles('c1ccccc1'::mol)")
    String getSampleSmiles();
}
