package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;
import com.tcmseek.common.core.domain.AjaxResult;
import com.tcmseek.common.utils.poi.ExcelUtil;
import com.tcmseek.pojo.vo.GraphDataVO;
import com.tcmseek.pojo.vo.GraphNodeVO;
import com.tcmseek.service.KnowledgeGraphNeo4jService;
import com.tcmseek.service.KnowledgeGraphService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识图谱控制器
 * @author TCMSeek
 */
@Anonymous
@Slf4j
@RestController
@RequestMapping("/tcmseek/knowledge-graph")
@Api(tags = "知识图谱接口")
public class KnowledgeGraphController {

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Autowired
    private KnowledgeGraphNeo4jService knowledgeGraphNeo4jService;

    /**
     * 构建知识图谱
     * @param centerType 中心节点类型: herb/prescription/compound/gene/disease/symptom/syndrome/wm_symptom
     * @param centerId 中心节点ID
     * @param relations 需要展示的关系类型，逗号分隔，如：compound,disease,syndrome
     * @return 图谱数据
     */
    @GetMapping("/build")
    @ApiOperation("构建知识图谱")
    public AjaxResult buildGraph(
            @ApiParam(value = "中心节点类型", required = true, example = "herb")
            @RequestParam("centerType") String centerType,
            
            @ApiParam(value = "中心节点ID", required = true, example = "HERB_1")
            @RequestParam("centerId") String centerId,
            
            @ApiParam(value = "关系类型（逗号分隔）", example = "compound,disease,syndrome")
            @RequestParam(value = "relations", required = false, defaultValue = "") String relations
    ) {
        try {
            log.info("构建知识图谱 - centerType: {}, centerId: {}, relations: {}", centerType, centerId, relations);
            
            // 验证中心节点类型
            if (!isValidCenterType(centerType)) {
                return AjaxResult.error("无效的中心节点类型，支持的类型：herb, prescription, compound, gene, disease, symptom, syndrome, wm_symptom");
            }
            
            // 解析关系类型
            String[] relationArray = relations.isEmpty() ? new String[0] : relations.split(",");
            
            // 构建图谱
            GraphDataVO graphData = knowledgeGraphService.buildGraph(centerType, centerId, relationArray);
            
            if (graphData == null || graphData.getNodes() == null || graphData.getNodes().isEmpty()) {
                return AjaxResult.error("未找到相关数据");
            }
            
            log.info("图谱构建成功 - 节点数: {}, 边数: {}", graphData.getNodeCount(), graphData.getEdgeCount());
            return AjaxResult.success(graphData);
            
        } catch (Exception e) {
            log.error("构建知识图谱失败", e);
            return AjaxResult.error("构建知识图谱失败: " + e.getMessage());
        }
    }

    /**
     * 搜索可用的中心节点
     * @param centerType 节点类型
     * @param keyword 搜索关键词
     * @param page 页码
     * @param pageSize 每页数量
     * @return 节点列表
     */
    @GetMapping("/search-center")
    @ApiOperation("搜索中心节点")
    public AjaxResult searchCenterNodes(
            @ApiParam(value = "节点类型", required = true)
            @RequestParam("centerType") String centerType,
            
            @ApiParam(value = "搜索关键词", required = true)
            @RequestParam("keyword") String keyword,
            
            @ApiParam(value = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            
            @ApiParam(value = "每页数量", example = "10")
            @RequestParam(value = "pageSize", defaultValue = "50") Integer pageSize
    ) {
        try {
            log.info("搜索中心节点 - type: {}, keyword: {}", centerType, keyword);
            
            if (!isValidCenterType(centerType)) {
                return AjaxResult.error("无效的节点类型");
            }
            
            return knowledgeGraphService.searchCenterNodes(centerType, keyword, page, pageSize);
            
        } catch (Exception e) {
            log.error("搜索中心节点失败", e);
            return AjaxResult.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 导出图谱数据
     */
    @GetMapping("/export")
    @ApiOperation("导出图谱数据")
    public void exportGraphData(HttpServletResponse response ,
                                @ApiParam(value = "中心节点类型", required = true, example = "herb")
                                @RequestParam("centerType") String centerType,

                                @ApiParam(value = "中心节点ID", required = true, example = "HERB_1")
                                @RequestParam("centerId") String centerId,

                                @ApiParam(value = "关系类型（逗号分隔）", example = "compound,disease,syndrome")
                                @RequestParam(value = "relations", required = false, defaultValue = "") String relations
    ) {
        try {
            log.info("构建知识图谱 - centerType: {}, centerId: {}, relations: {}", centerType, centerId, relations);

            // 验证中心节点类型
            if (!isValidCenterType(centerType)) {
                //中断
                return ;
            }

            // 解析关系类型
            String[] relationArray = relations.isEmpty() ? new String[0] : relations.split(",");

            // 构建图谱
            GraphDataVO graphData = knowledgeGraphService.buildGraph(centerType, centerId, relationArray);

            if (graphData == null || graphData.getNodes() == null || graphData.getNodes().isEmpty()) {
                return ;
            }

            log.info("图谱构建成功 - 节点数: {}, 边数: {}", graphData.getNodeCount(), graphData.getEdgeCount());


            String fileName = "knowledge-graph-" + centerType + "-" + centerId + ".json";

            List<GraphNodeVO> list = graphData.getNodes().stream()
                    .map(node -> new GraphNodeVO(node.getId(), node.getLabel(), node.getType(), node.getIsCenter(), node.getExtra()))
                    .collect(Collectors.toList());

            ExcelUtil<GraphNodeVO> util = new ExcelUtil<>(GraphNodeVO.class);
            util.exportEasyExcel(response, list, fileName);
            return ;

        } catch (Exception e) {
            log.error("构建知识图谱失败", e);
            return ;
        }

    }

    /**
     * 验证中心节点类型是否有效
     */
    private boolean isValidCenterType(String centerType) {
        return "herb".equals(centerType) 
            || "prescription".equals(centerType) 
            || "compound".equals(centerType) 
            || "gene".equals(centerType)
            || "disease".equals(centerType)
            || "symptom".equals(centerType)
            || "syndrome".equals(centerType)
            || "wm_symptom".equals(centerType);
    }
}


