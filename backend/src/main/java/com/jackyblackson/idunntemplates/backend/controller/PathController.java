package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.PathDto;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.backend.service.PathService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/paths")
@RequiredArgsConstructor
public class PathController {

    private final PathService pathService;
    private final LuckyPermAuthService luckyPermAuthService;

    /**
     * 获取指定路径下的下一级目录
     * 使用 {*path} 捕获包括斜杠在内的所有剩余路径
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<List<PathDto>> getNextNodes(
            @RequestParam(required = false, defaultValue = "") String path,
            UserContext user
    ) {
        // 处理 Spring 捕获路径时可能带有的前导斜杠
        String cleanPath = sanitizePath(path);

        List<String> nextLevels = pathService.getNextLevelDirectories(cleanPath);

        // 批量鉴权
        List<String> fullPaths = new ArrayList<>();
        for (String dirName : nextLevels) {
            String fullPath = cleanPath.isEmpty() ? dirName : cleanPath + "/" + dirName;
            fullPaths.add(fullPath);
        }

        List<String> allPerms = new ArrayList<>();
        for (String fp : fullPaths) {
            String dotPath = fp.replace("/", ".");
            allPerms.add(PermissionNames.Templates.createInPath$R + "." + dotPath);
            allPerms.add(PermissionNames.Templates.commitToPath$R + "." + dotPath);
            allPerms.add(PermissionNames.Templates.usePath$R + "." + dotPath);
        }

        Map<String, Boolean> results = luckyPermAuthService.batchCheckPermissions(user.getUuid(), user.getUsername(), allPerms);

        List<PathDto> dtos = new ArrayList<>();
        for (int i = 0; i < nextLevels.size(); i++) {
            String dirName = nextLevels.get(i);
            String fullPath = fullPaths.get(i);
            String dotPath = fullPath.replace("/", ".");

            boolean canSave = results.getOrDefault(PermissionNames.Templates.createInPath$R + "." + dotPath, false);
            boolean canCommit = results.getOrDefault(PermissionNames.Templates.commitToPath$R + "." + dotPath, false);
            boolean canUse = results.getOrDefault(PermissionNames.Templates.usePath$R + "." + dotPath, false);

            dtos.add(new PathDto(dirName, canSave, canCommit, canUse));
        }

        return ResponseEntity.ok(dtos);
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
