package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.commercial.dto.netease.NeteaseOrderDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrder;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseOrderStatus;
import com.jackyblackson.idunntemplates.backend.commercial.repository.netease.NeteaseOrderRepository;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RESTful API for managing NeteaseOrder entities.
 */
@RestController
@RequestMapping("/api/v1/commercial")
@AllArgsConstructor
public class NeteaseOrderController {

    private final NeteaseOrderRepository orderRepository;

    /**
     * 查询指定商品下的订单列表，支持分页和动态筛选。
     *
     * @param productId 商品ID（路径变量）
     * @param search    查询条件字符串，格式：字段:值 或 字段~:值（模糊查询），多个条件用逗号分隔。
     *                  例如：internalStatus:ENTERED,appOrderId~:TEST,product.id:123
     * @param pageable  分页参数，默认页码0，每页20条，按id降序排序。
     * @return 分页结果
     */
    @GetMapping("/netease-products/{productId}/orders")
    public ResponseEntity<Page<NeteaseOrderDto>> listByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // 构建查询条件：强制加上 product.id = productId，再合并其他筛选条件
        Specification<NeteaseOrder> spec = buildSpecification(search)
                .and((root, query, cb) -> cb.equal(root.get("product").get("id"), productId));

        Page<NeteaseOrder> page = orderRepository.findAll(spec, pageable);
        Page<NeteaseOrderDto> dtoPage = page.map(this::convertToDto);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 全局查询订单列表（不限定商品），支持动态筛选。
     *
     * @param search   查询条件字符串
     * @param pageable 分页参数
     * @return 分页结果
     */
    @GetMapping("/netease-orders")
    public ResponseEntity<Page<NeteaseOrderDto>> listAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Specification<NeteaseOrder> spec = buildSpecification(search);
        Page<NeteaseOrder> page = orderRepository.findAll(spec, pageable);
        Page<NeteaseOrderDto> dtoPage = page.map(this::convertToDto);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 根据ID查询单个订单详情。
     *
     * @param id 订单ID
     * @return 订单信息，若不存在返回404
     */
    @GetMapping("/netease-orders/{id}")
    public ResponseEntity<NeteaseOrderDto> getById(@PathVariable Long id) {
        Optional<NeteaseOrder> optional = orderRepository.findById(id);
        return optional.map(order -> ResponseEntity.ok(convertToDto(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- 辅助方法 ----------

    /**
     * 将 NeteaseOrder 实体转换为 DTO。
     */
    private NeteaseOrderDto convertToDto(NeteaseOrder order) {
        NeteaseOrderDto dto = new NeteaseOrderDto();
        dto.setId(order.getId());
        dto.setNePeProductLogId(order.getNePeProductLogId());
        dto.setAppOrderId(order.getAppOrderId());
        dto.setAppOrderIdInt(order.getAppOrderIdInt());
        dto.setAppUid(order.getAppUid());
        dto.setAppUidInt(order.getAppUidInt());
        dto.setDiscount(order.getDiscount());
        dto.setOfficialChannel(order.getOfficialChannel());
        dto.setPoint(order.getPoint());
        dto.setPointType(order.getPointType());
        dto.setPrice(order.getPrice());
        dto.setPriceType(order.getPriceType());
        dto.setProductName(order.getProductName());
        dto.setPurchaseLimit(order.getPurchaseLimit());
        dto.setRefundStatus(order.getRefundStatus());
        dto.setShipTime(order.getShipTime());
        dto.setShipTimeMs(order.getShipTimeMs());
        dto.setRefundInTimeMs(order.getRefundInTimeMs());
        dto.setInternalStatus(order.getInternalStatus() != null ? order.getInternalStatus().name() : null);
        // 关联商品信息（可选）
        if (order.getProduct() != null) {
            dto.setProductId(order.getProduct().getId());
        }
        return dto;
    }

    /**
     * 构建动态查询 Specification。
     * 解析 search 字符串，生成对应的查询条件（AND 连接）。
     *
     * @param search 查询字符串
     * @return Specification 对象，如果 search 为空则返回空 Specification（即无条件）
     */
    private Specification<NeteaseOrder> buildSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return Specification.where(null); // 返回无条件规格
        }

        List<SearchCriteria> criteriaList = parseSearch(search);

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                Path<?> path = resolvePath(root, criteria.getField());
                if (path == null) continue; // 无法解析的字段忽略
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
                    // 可扩展其他操作符
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 解析字段路径（支持点号嵌套），例如 "product.id" 会返回 root.get("product").get("id")。
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
        } else if (targetType.isEnum()) {
            // 枚举类型，根据字符串转换为枚举常量
            return Enum.valueOf((Class<Enum>) targetType, value);
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