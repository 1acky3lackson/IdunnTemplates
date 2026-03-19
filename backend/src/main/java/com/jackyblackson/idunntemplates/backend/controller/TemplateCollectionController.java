package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.domain.TemplateCollection;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.TemplateCollectionService;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateCollectionRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collections")
@AllArgsConstructor
public class TemplateCollectionController {

    private final TemplateCollectionRepository repository;
    private final TemplateCollectionService collectionService;

    // ... 在 TemplateCollectionController 顶部添加 TemplateRepository 依赖 ...
    private final TemplateRepository templateRepository;

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
        collectionService.deleteCollection(id, user, viewAll);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取指定合集下的 Template 列表
     * 支持对 Template 表字段的 search、分页和排序
     */
    @GetMapping("/{id}/templates")
    @AuthRequired
    public ResponseEntity<Page<Template>> listTemplatesInCollection(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            @PageableDefault(size = 20, sort = "lastVersionAt", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user) {

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

        return ResponseEntity.ok(page);
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
        collectionService.removeTemplateFromCollection(id, templateId, user, viewAll);
        return ResponseEntity.noContent().build();
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