package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.domain.TemplateCollection;
import com.jackyblackson.idunntemplates.backend.dto.TrustedServerContext;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.TemplateCollectionService;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateCollectionRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.backend.dto.TemplateWithColorsDto;
import com.jackyblackson.idunntemplates.backend.service.TemplateColorService;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import org.springframework.data.domain.PageImpl;
import com.jackyblackson.idunntemplates.core.domain.Template;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/collections")
@AllArgsConstructor
public class TemplateCollectionController {

    private final TemplateCollectionRepository repository;
    private final TemplateCollectionService collectionService;

    // ... 在 TemplateCollectionController 顶部添加 TemplateRepository 和其他所需依赖 ...
    private final TemplateRepository templateRepository;
    private final TemplateColorService templateColorService;
    private final LuckyPermAuthService luckyPermAuthService;
    private final TemplateVersionRepository templateVersionRepository;

    /**
     * 分页查询合集列表
     * 支持 search 参数动态查询。默认只返回公开合集和当前用户的私有合集。
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<TemplateCollection>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user
    ) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.list)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        boolean viewAll = false;

        // 1. 构建搜索条件的 Specification
        Specification<TemplateCollection> searchSpec = buildSpecification(search);

        // 2. 构建可见性规则的 Specification
        Specification<TemplateCollection> visibilitySpec = (root, query, criteriaBuilder) -> {
            if (viewAll) {
                // 如果是 viewAll (如管理员)，无可见性限制
                return criteriaBuilder.conjunction();
            }
            // 普通用户：仅返回 isPrivate = false 的，或者 creatorName 是自己的
            Predicate isPublic = criteriaBuilder.isFalse(root.get("privateCollection"));
            Predicate isOwner = criteriaBuilder.equal(root.get("creatorName"), user.getUsername());
            return criteriaBuilder.or(isPublic, isOwner);
        };

        // 3. 组合 Specification
        Specification<TemplateCollection> finalSpec = searchSpec == null
                ? visibilitySpec
                : searchSpec.and(visibilitySpec);

        Page<TemplateCollection> page = repository.findAll(finalSpec, pageable);

        // 此处直接返回 Entity，如果需要脱敏或隐藏字段，建议转换为对应的 Dto
        return ResponseEntity.ok(page);
    }

    /**
     * 获取单个合集详情
     */
    @GetMapping("/{id}")
    @AuthRequired
    public ResponseEntity<TemplateCollection> getById(
            @PathVariable Long id,
            UserContext user
    ) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.get)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        boolean viewAll = false;

        TemplateCollection collection = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

        // 权限校验：如果不是 viewAll，且是私有的，且不是自己创建的，则拒绝访问
        if (!viewAll && collection.isPrivateCollection() && !collection.getCreatorName().equals(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        return ResponseEntity.ok(collection);
    }

    /**
     * 新建合集
     */
    @PostMapping
    @AuthRequired
    public ResponseEntity<TemplateCollection> create(
            @RequestBody CollectionRequest request,
            UserContext user
    ) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.create)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        TemplateCollection created = collectionService.createCollection(
                request.getName(), request.getDescription(), request.isPrivateCollection(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新合集信息
     */
    @PutMapping("/{id}")
    @AuthRequired
    public ResponseEntity<TemplateCollection> update(
            @PathVariable Long id,
            @RequestBody CollectionRequest request,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            UserContext user
    ) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.update)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        TemplateCollection updated = collectionService.updateCollection(
                id, request.getName(), request.getDescription(), request.isPrivateCollection(), user, viewAll);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除合集
     */
    @DeleteMapping("/{id}")
    @AuthRequired
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            UserContext user
    ) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.delete)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        collectionService.deleteCollection(id, user, viewAll);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取指定合集下的 Template 列表
     * 支持对 Template 表字段的 search、分页和排序
     */
    @GetMapping("/{id}/templates")
    @AuthRequired
    public ResponseEntity<Page<TemplateWithColorsDto>> listTemplatesInCollection(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            @PageableDefault(size = 20, sort = "lastVersionAt", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user) {

        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.listTemplates)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        // 1. 获取针对该合集的子查询过滤条件（内部已包含合集可见性鉴权）
        Specification<Template> collectionSpec = collectionService.getTemplatesInCollectionSpec(id, user, viewAll);

        // 2. 解析前端传来的 search 字符串，构造针对 Template 的动态查询条件
        Specification<Template> searchSpec = buildTemplateSpecification(search);

        // 3. 合并条件：必须属于该合集 AND 满足搜索条件
        Specification<Template> finalSpec = searchSpec == null
                ? collectionSpec
                : collectionSpec.and(searchSpec);

        // 4. 执行查询
        Page<Template> page = templateRepository.findAll(finalSpec, pageable);
        List<Template> originalContent = page.getContent();

        // 1. 提取非空的 ID 和 Path 用于批量查询，避免对 null 对象调用方法
        List<Template> nonNullTemplates = originalContent.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2. 批量解析颜色 (Map 的 Key 是 UUID)
        Map<UUID, List<String>> colors = nonNullTemplates.isEmpty() ? Collections.emptyMap() :
                templateColorService.resolveColorsForTemplates(nonNullTemplates);

        // 3. 批量检查权限 (需处理 path 为 null 的情况)
        List<String> distinctPaths = nonNullTemplates.stream()
                .map(Template::getPath)
                .filter(Objects::nonNull)
                .map(path -> PermissionNames.Templates.commitToPath$R + "." + path.replace("/", "."))
                .distinct()
                .collect(Collectors.toList());

        Map<String, Boolean> commitPermResults = (user != null && !distinctPaths.isEmpty()) ?
                luckyPermAuthService.batchCheckPermissions(user.getUuid(), user.getUsername(), distinctPaths) :
                Collections.emptyMap();

        // 4. 批量获取版本 (关联查询)
        Map<UUID, List<TemplateVersion>> versionsMap = nonNullTemplates.isEmpty() ? Collections.emptyMap() :
                templateVersionRepository.findByTemplateIn(nonNullTemplates).stream()
                        .filter(v -> v != null && v.getTemplate() != null)
                        .collect(Collectors.groupingBy(v -> v.getTemplate().getId()));

        // 5. 映射 DTO，严格保持 originalContent 的顺序和长度
        List<TemplateWithColorsDto> dtos = originalContent.stream().map(t -> {
            // 如果元素为 null，直接返回 null 保持占位
            if (t == null) {
                return null;
            }

            // 此时 t 保证非空
            List<String> colorList = colors.getOrDefault(t.getId(), Collections.emptyList());
            TemplateWithColorsDto dto = new TemplateWithColorsDto(t, colorList);

            // 处理版本信息
            List<TemplateVersion> allVersions = versionsMap.getOrDefault(t.getId(), Collections.emptyList());
            List<TemplateVersion> sortedVersions = allVersions.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingLong((TemplateVersion v) ->
                            Optional.of(v.getCreatedAt()).orElse(0L)).reversed())
                    .toList();

            dto.setVersionCount(sortedVersions.size());
            if (!sortedVersions.isEmpty()) {
                dto.setLatestVersionName(sortedVersions.get(0).getVersionId());
                dto.setLatestVersions(sortedVersions.stream().limit(10).collect(Collectors.toList()));
            } else {
                dto.setLatestVersions(Collections.emptyList());
            }

            dto.setCanUse(true);

            // 处理权限
            String path = t.getPath();
            if (path != null) {
                String commitPerm = PermissionNames.Templates.commitToPath$R + "." + path.replace("/", ".");
                dto.setCanCommit(commitPermResults.getOrDefault(commitPerm, false));
            } else {
                dto.setCanCommit(false);
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(dtos, page.getPageable(), page.getTotalElements()));
    }

    /**
     * 专门用于解析 Template 实体的 Specification
     * 逻辑与解析 Collection 的一致，但作用域为 Template
     */
    private Specification<Template> buildTemplateSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        List<SearchCriteria> criteriaList = parseSearch(search); // 复用之前的解析方法

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                Path<?> path = resolvePath(root, criteria.getField());
                if (path == null) continue;

                Object value;
                // 针对 Template 实体的特殊字段做类型转换，例如 UUID
                if (path.getJavaType() == UUID.class) {
                    try {
                        value = UUID.fromString(criteria.getValue());
                    } catch (IllegalArgumentException e) {
                        continue; // 非法 UUID 格式直接忽略该条件
                    }
                } else {
                    value = convertValue(path.getJavaType(), criteria.getValue());
                }

                switch (criteria.getOperator()) {
                    case EQ:
                        predicates.add(criteriaBuilder.equal(path, value));
                        break;
                    case LIKE:
                        if (value instanceof String) {
                            predicates.add(criteriaBuilder.like((Path<String>) path, "%" + value + "%"));
                        }
                        break;
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 向合集增加 Template
     */
    @PostMapping("/{id}/templates")
    @AuthRequired
    public ResponseEntity<Void> addTemplate(
            @PathVariable Long id,
            @RequestBody TemplateActionRequest request,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            UserContext user
    ) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.addTemplate)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        collectionService.addTemplateToCollection(id, request.getTemplateId(), user, viewAll);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 从合集删除 Template
     */
    @DeleteMapping("/{id}/templates/{templateId}")
    @AuthRequired
    public ResponseEntity<Void> removeTemplate(
            @PathVariable Long id,
            @PathVariable UUID templateId,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            UserContext user
    ) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.removeTemplate)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        collectionService.removeTemplateFromCollection(id, templateId, user, viewAll);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取指定合集下的随机模板
     * 采用外尔序列算法尽力避免重复
     */
    @GetMapping("/{id}/random")
    @AuthRequired(allowServerToken = true)
    public ResponseEntity<TemplateWithColorsDto> getRandomTemplate(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            UserContext user,
            TrustedServerContext trustedServerContext) {
        
        if (trustedServerContext != null) {
            viewAll = true;
        } else {
            if (!luckyPermAuthService.checkPermission(user, PermissionNames.TemplateCollections.getRandom)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
            }
        }

        Template t = collectionService.getRandomTemplateFromCollection(id, user, viewAll);
        if (t == null) {
            return ResponseEntity.notFound().build();
        }

        // 映射 DTO (颜色、版本、权限)
        Map<UUID, List<String>> colors = templateColorService.resolveColorsForTemplates(Collections.singletonList(t));
        List<String> colorList = colors.getOrDefault(t.getId(), Collections.emptyList());

        TemplateWithColorsDto dto = new TemplateWithColorsDto(t, colorList);

        // 处理版本信息
        List<TemplateVersion> allVersions = templateVersionRepository.findByTemplateIn(Collections.singletonList(t));
        List<TemplateVersion> sortedVersions = allVersions.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong((TemplateVersion v) ->
                        Optional.ofNullable(v.getCreatedAt()).orElse(0L)).reversed())
                .collect(Collectors.toList());

        dto.setVersionCount(sortedVersions.size());
        if (!sortedVersions.isEmpty()) {
            dto.setLatestVersionName(sortedVersions.get(0).getVersionId());
            dto.setLatestVersions(sortedVersions.stream().limit(10).collect(Collectors.toList()));
        } else {
            dto.setLatestVersions(Collections.emptyList());
        }

        dto.setCanUse(true);

        // 处理权限
        String path = t.getPath();
        if (path != null) {
            String commitPerm = PermissionNames.Templates.commitToPath$R + "." + path.replace("/", ".");
            Map<String, Boolean> permResult = luckyPermAuthService.batchCheckPermissions(
                    user.getUuid(), user.getUsername(), Collections.singletonList(commitPerm));
            dto.setCanCommit(permResult.getOrDefault(commitPerm, false));
        } else {
            dto.setCanCommit(false);
        }

        return ResponseEntity.ok(dto);
    }

    // ---------- 内部 Request DTO 类 ----------

    @Getter
    public static class CollectionRequest {
        private String name;
        private String description;
        private boolean privateCollection;
    }

    @Getter
    public static class TemplateActionRequest {
        private UUID templateId;
    }

    // ---------- Specification 查询构造逻辑 (仿照原有结构) ----------

    private Specification<TemplateCollection> buildSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        List<SearchCriteria> criteriaList = parseSearch(search);

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                Path<?> path = resolvePath(root, criteria.getField());
                if (path == null) continue;
                Object value = convertValue(path.getJavaType(), criteria.getValue());

                switch (criteria.getOperator()) {
                    case EQ:
                        predicates.add(criteriaBuilder.equal(path, value));
                        break;
                    case LIKE:
                        if (value instanceof String) {
                            predicates.add(criteriaBuilder.like((Path<String>) path, "%" + value + "%"));
                        }
                        break;
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Path<?> resolvePath(Path<?> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private List<SearchCriteria> parseSearch(String search) {
        List<SearchCriteria> list = new ArrayList<>();
        String[] conditions = search.split(",");
        for (String condition : conditions) {
            String[] parts = condition.split(":", 2);
            if (parts.length != 2) continue;

            String fieldWithOp = parts[0].trim();
            String value = parts[1].trim();

            Operator op = Operator.EQ;
            String field = fieldWithOp;
            if (fieldWithOp.endsWith("~")) {
                op = Operator.LIKE;
                field = fieldWithOp.substring(0, fieldWithOp.length() - 1).trim();
            }

            if (!field.isEmpty() && !value.isEmpty()) {
                list.add(new SearchCriteria(field, op, value));
            }
        }
        return list;
    }

    private Object convertValue(Class<?> targetType, String value) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    @Getter
    private static class SearchCriteria {
        private final String field;
        private final Operator operator;
        private final String value;

        public SearchCriteria(String field, Operator operator, String value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
    }

    private enum Operator {
        EQ, LIKE
    }
}