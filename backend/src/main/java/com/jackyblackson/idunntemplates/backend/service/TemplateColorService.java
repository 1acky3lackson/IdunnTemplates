package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.domain.TemplateColorScheme;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateColorSchemeRepository;
import com.jackyblackson.idunntemplates.backend.util.CollectionUtils;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TemplateColorService {

    private final TemplateColorSchemeRepository repository;
    private final TemplateColorGenerator colorGenerator;

    private final TemplateVersionService templateVersionService;

    public TemplateColorService(TemplateColorSchemeRepository repository, TemplateColorGenerator colorGenerator, TemplateVersionService templateVersionService) {
        this.repository = repository;
        this.colorGenerator = colorGenerator;
        this.templateVersionService = templateVersionService;
    }

    /**
     * 使用工具类重写：批量获取模板颜色
     */
    public Map<UUID, List<String>> resolveColorsForTemplates(List<Template> templates) {
        List<String> defaultColors = Collections.nCopies(6, "unknown");

        return CollectionUtils.resolveBatch(
                templates,
                Template::getId,
                (ids) -> {
                    // 1. 批量从数据库查询已有的缓存
                    List<TemplateColorScheme> stored = repository.findByTemplateIdIn(ids);
                    Map<UUID, TemplateColorScheme> schemeMap = stored.stream()
                            .collect(Collectors.toMap(TemplateColorScheme::getTemplateId, s -> s));

                    // 2. 构造本次批量的结果 Map
                    Map<UUID, List<String>> batchResult = new HashMap<>();

                    // 这里需要根据 ID 找回原来的 Template 对象来判断版本
                    // 技巧：为了性能，我们可以先在外面把 templates 转成 map 方便查找
                    Map<UUID, Template> templateMap = templates.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toMap(Template::getId, t -> t, (v1, v2) -> v1));

                    for (UUID id : ids) {
                        Template t = templateMap.get(id);
                        TemplateColorScheme scheme = schemeMap.get(id);

                        var latestOptional = templateVersionService.getLatestVersion(t.getId());
                        String currentVersion = latestOptional.isPresent() ? latestOptional.get().getVersionId() : "unknown";

                        if (scheme != null && currentVersion.equals(scheme.getVersion())) {
                            batchResult.put(id, Arrays.asList(scheme.getColors().split(",")));
                        } else {
                            // 触发生成（已解决循环依赖，直接调 generator）
                            batchResult.put(id, colorGenerator.generateColorScheme(t));
                        }
                    }
                    return batchResult;
                },
                defaultColors // 默认值
        );
    }

    @Transactional(readOnly = true)
    public List<TemplateColorScheme> getStoredSchemes(List<UUID> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) return Collections.emptyList();
        return repository.findByTemplateIdIn(templateIds);
    }

    @Transactional(readOnly = true)
    public Optional<TemplateColorScheme> getStoredScheme(UUID templateId) {
        return repository.findByTemplateId(templateId);
    }
}