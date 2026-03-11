package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.commercial.dto.netease.NeteaseProductDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProductStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseProductRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.ProductPermissionService;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RESTful API for managing NeteaseProduct entities.
 */
@RestController
@RequestMapping("/api/v1/commercial/netease-products")
@AllArgsConstructor
public class NeteaseProductController {

    private NeteaseProductRepository repository;
    private final ProductPermissionService permissionService;  // 注入权限服务

    /**
     * 分页查询商品列表，支持通过 search 参数构建动态查询条件。
     *
     * @param search   查询条件字符串，格式：字段:值 或 字段~:值（模糊查询），多个条件用逗号分隔。
     *                 例如：status:CREATED,itemName~:测试,price:100
     * @param pageable 分页参数，默认页码0，每页20条，按id升序排序。
     * @return 分页结果
     */
    @GetMapping
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
                Path<?> path = root.get(criteria.getField());
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
                    // 可以扩展其他操作符，如 GT, LT, IN 等
                    default:
                        // ignore
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
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
        EQ,      // 等于
        LIKE     // 模糊查询
    }
}