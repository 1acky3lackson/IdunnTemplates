package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PathService {

    private final TemplateRepository templateRepository;

    /**
     * 根据前缀路径，获取下一级所有可能的目录名
     * * @param prefixPath 输入的前缀，例如 "a" 或 "a/"
     * @return 下一级目录的名称列表
     */
    public List<String> getNextLevelDirectories(String prefixPath) {
        // 1. 规范化路径：确保以 / 结尾
        String tailedPath;
        if (prefixPath == null || prefixPath.isEmpty() || prefixPath.equals("/")) {
            tailedPath = "";
        } else if (prefixPath.endsWith("/")){
            tailedPath = prefixPath;
        } else {
            tailedPath = prefixPath + "/";
        }
        String normalizedPath;

        if(!tailedPath.startsWith("/")) normalizedPath = "/" + tailedPath;
        else normalizedPath = tailedPath;

        // 2. 从数据库查询所有以该路径开头的模板
        List<Template> templates = templateRepository.findByPathStartingWith(tailedPath);

        // 3. 提取逻辑
        return templates.stream()
                .map(t -> "/" + t.getPath())
                // 截取掉前缀部分，例如 "a/b/x" 变成 "b/x"
                .map(path -> path.substring(normalizedPath.length()))
                // 过滤掉空字符串（防止刚好路径一致的情况）
                .filter(remaining -> !remaining.isEmpty())
                // 寻找剩余部分中的第一个 '/'
                .map(remaining -> {
                    int firstSlash = remaining.indexOf("/");
                    // 如果找不到 '/'，说明剩下的部分就是一个文件名，根据需求不返回文件
                    if (firstSlash == -1) {
                        return null;
                    }
                    // 返回下一级目录名，例如 "b/x" 返回 "b"
                    return normalizedPath + remaining.substring(0, firstSlash);
                })
                .filter(Objects::nonNull)
                .distinct() // 去重
                .map(p -> p.substring(1))
                .collect(Collectors.toList());
    }
}