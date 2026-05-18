package com.tcmseek.controller;

import com.tcmseek.common.annotation.Anonymous;

import com.tcmseek.pojo.entity.GraphResult;

import com.tcmseek.service.KnowledgeGraphNeo4jService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tcmseek/knowledge-graph-neo4j")
@Api(tags = "知识图谱接口")
@Anonymous
public class KnowledgeGraphNeo4jController {

    @Autowired
    private  KnowledgeGraphNeo4jService knowledgeGraphService;

    @GetMapping("/build")
    @ApiOperation("构建知识图谱")
    public ResponseEntity<GraphResult> buildGraph(
            @RequestParam String centerType,
            @RequestParam String centerId,
            @RequestParam String relations) {

        return ResponseEntity.ok(
                knowledgeGraphService.buildKnowledgeGraph(centerType, centerId, relations)
        );
    }
}
