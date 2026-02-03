package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.PathService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;
import java.util.List;

@RestController
@RequestMapping("/api/v1/paths")
@RequiredArgsConstructor
public class PathController {

    private final PathService pathService;

    /**
     * 获取指定路径下的下一级目录
     * 使用 {*path} 捕获包括斜杠在内的所有剩余路径
     */
    @GetMapping("/{*path}")
//    @AuthRequired
    public ResponseEntity<List<String>> getNextNodes(
            @PathVariable("path") String path//,
//            UserContext user
    ) {
        // 处理 Spring 捕获路径时可能带有的前导斜杠
        String cleanPath = sanitizePath(path);

        List<String> nextLevels = pathService.getNextLevelDirectories(cleanPath);
        return ResponseEntity.ok(nextLevels);
    }

    /**
     * 辅助方法：清洗路径参数
     * Spring 的 {*path} 可能会把 "/a/b" 捕获为 "/a/b" 或 "a/b"
     */
    private String sanitizePath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return "";
        }
        // 去除开头的斜杠，确保 service 逻辑一致性
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
