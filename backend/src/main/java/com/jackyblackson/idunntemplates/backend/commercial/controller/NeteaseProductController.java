package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.dto.netease.NeteaseProductDto;
import com.jackyblackson.idunntemplates.backend.commercial.dto.netease.ProductOrderStatsDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProductStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.ProjectRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseProductRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseProductStatService;
import com.jackyblackson.idunntemplates.backend.commercial.service.ProductPermissionService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import jakarta.persistence.Column;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RESTful API for managing NeteaseProduct entities.
 */
@RestController
@RequestMapping("/api/v1/commercial/products")
@AllArgsConstructor
public class NeteaseProductController {

    private final LuckyPermAuthService luckyPermAuthService;
    private final NeteaseProductStatService neteaseProductStatService;
    private NeteaseProductRepository repository;
    private final ProductPermissionService permissionService; // 注入权限服务
    private final ProjectRepository projectRepository; // 新增依赖，用于指定项目

    /**
     * 获取指定商品的订单统计数据
     *
     * @param id 商品ID
     * @return 订单统计数据 DTO
     */
    @GetMapping("/{id}/stats")
    @AuthRequired
    public ResponseEntity<ProductOrderStatsDto> getProductStats(@PathVariable Long id) {
        // 1. 先校验商品是否存在，保持与 update/changeStatus 接口一致的 404 处理风格
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        // 2. 调用 Service 进行计算
        ProductOrderStatsDto stats = neteaseProductStatService.calculateProductStats(id);

        return ResponseEntity.ok(stats);
    }

    /**
     * 更新商品的部分字段（例如指定项目、修改状态）。
     *
     * @param id      商品ID
     * @param request 更新请求体
     * @return 更新后的商品信息
     */
    @PutMapping("/{id}")
    @Transactional
    @AuthRequired
    public ResponseEntity<NeteaseProductDto> update(@PathVariable Long id,
            @RequestBody NeteaseProductUpdateRequest request,
            UserContext user) {

        if (!luckyPermAuthService.checkPermission(user, PermissionNames.Commercial.Product.modify)) {
            return ResponseEntity.status(406).build();
        }

        NeteaseProduct product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 更新 project 关联
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Project not found with id: " + request.getProjectId()));
            product.setProject(project);
        } else if (request.isClearProject()) { // 如果明确要求清空项目
            product.setProject(null);
        }

        // 更新状态
        if (request.getInternalStatus() != null) {
            product.setInternalStatus(request.getInternalStatus());
        }

        // 更新修改时间（如果有）
        product.setUpdateTimeMs(System.currentTimeMillis());

        NeteaseProduct saved = repository.save(product);
        return ResponseEntity.ok(permissionService.toDto(saved));
    }

    /**
     * 仅为商品指定项目（更语义化的端点）。
     */
    @PatchMapping("/{id}/project")
    @Transactional
    @AuthRequired
    public ResponseEntity<NeteaseProductDto> assignProject(@PathVariable Long id,
            @RequestBody ProjectAssignmentRequest request,
            UserContext user) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.Commercial.Product.bindProject)) {
            return ResponseEntity.status(406).build();
        }
        NeteaseProduct product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (request.getProjectId() == null) {
            product.setProject(null);
        } else {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Project not found with id: " + request.getProjectId()));
            product.setProject(project);
        }
        product.setUpdateTimeMs(System.currentTimeMillis());
        return ResponseEntity.ok(permissionService.toDto(repository.save(product)));
    }

    /**
     * 修改商品状态。
     */
    @PatchMapping("/{id}/status")
    @Transactional
    @AuthRequired
    public ResponseEntity<NeteaseProductDto> changeStatus(@PathVariable Long id,
            @RequestBody StatusChangeRequest request,
            UserContext user) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.Commercial.Product.changeStatus)) {
            return ResponseEntity.status(406).build();
        }
        NeteaseProduct product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (request.getInternalStatus() != null) {
            product.setInternalStatus(request.getInternalStatus());
        }
        product.setUpdateTimeMs(System.currentTimeMillis());
        return ResponseEntity.ok(permissionService.toDto(repository.save(product)));
    }

    // ---------- 内部 DTO 类 ----------

    /**
     * 通用的商品更新请求（支持部分字段）。
     */
    @Getter
    public static class NeteaseProductUpdateRequest {
        private Long projectId; // 要关联的项目ID，null 表示不修改
        private boolean clearProject = false; // 是否清空项目（当为 true 时，projectId 忽略）
        private NeteaseProductStatus internalStatus; // 要更新的状态，null 表示不修改
        // 可扩展其他字段
    }

    /**
     * 项目指派请求（专用于设置项目）。
     */
    @Getter
    public static class ProjectAssignmentRequest {
        private Long projectId; // 项目ID，null 表示清空项目
    }

    /**
     * 状态修改请求。
     */
    @Getter
    public static class StatusChangeRequest {
        private NeteaseProductStatus internalStatus;
    }

    /**
     * 分页查询商品列表，支持通过 search 参数构建动态查询条件。
     *
     * @param search   查询条件字符串，格式：字段:值 或 字段~:值（模糊查询），多个条件用逗号分隔。
     *                 例如：status:CREATED,itemName~:测试,price:100
     * @param pageable 分页参数，默认页码0，每页20条，按id升序排序。
     * @return 分页结果
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<NeteaseProductDto>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Specification<NeteaseProduct> spec = buildSpecification(search);
        Page<NeteaseProduct> page = repository.findAll(spec, pageable);

        // 将 Page 中的内容转换为 DTO（保留分页信息）
        Page<NeteaseProductDto> dtoPage = page.map(permissionService::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 根据ID查询单个商品。
     *
     * @param id 商品ID
     * @return 商品信息，若不存在返回404
     */
    @GetMapping("/{id}")
    @AuthRequired
    public ResponseEntity<NeteaseProductDto> getById(@PathVariable Long id) {
        Optional<NeteaseProduct> optional = repository.findById(id);
        return optional.map(entity -> ResponseEntity.ok(permissionService.toDto(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 构建动态查询 Specification。
     * 解析 search 字符串，生成对应的查询条件（AND 连接）。
     *
     * @param search 查询字符串
     * @return Specification 对象，如果 search 为空则返回 null（查询所有）
     */
    private Specification<NeteaseProduct> buildSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }

        List<SearchCriteria> criteriaList = parseSearch(search);

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                Path<?> path = resolvePath(root, criteria.getField());
                if (path == null)
                    continue; // 无法解析的字段忽略
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
                    default:
                        // ignore
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 解析字段路径（支持点号嵌套），例如 "project.id" 会返回 root.get("project").get("id")。
     */
    private Path<?> resolvePath(Path<?> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    /**
     * 解析 search 字符串为 SearchCriteria 对象列表。
     * 格式：字段名:值（等值查询），字段名~:值（模糊查询），多个条件用逗号分隔。
     */
    private List<SearchCriteria> parseSearch(String search) {
        List<SearchCriteria> list = new ArrayList<>();
        String[] conditions = search.split(",");
        for (String condition : conditions) {
            String[] parts = condition.split(":", 2);
            if (parts.length != 2)
                continue;

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

    /**
     * 将字符串值转换为字段对应的 Java 类型。
     */
    private Object convertValue(Class<?> targetType, String value) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == NeteaseProductStatus.class) {
            // 枚举类型，根据字符串转换为枚举常量
            return NeteaseProductStatus.valueOf(value);
        } else {
            // 其他类型默认按字符串处理
            return value;
        }
    }

    /**
     * 内部类：表示一个查询条件
     */
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

    /**
     * 操作符枚举
     */
    private enum Operator {
        EQ, // 等于
        LIKE // 模糊查询
    }
}