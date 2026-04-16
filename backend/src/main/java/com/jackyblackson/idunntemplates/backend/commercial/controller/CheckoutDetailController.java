package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/commercial/profits")
@AllArgsConstructor
public class CheckoutDetailController {

    private final CheckoutDetailRepository checkoutDetailRepository;

    /**
     * 搜索结算记录，支持动态条件与分页
     *
     * @param search   查询条件，格式：字段:值 或 字段~:值（模糊查询），多个条件用逗号分隔
     *                 例如：username:player1,status:CREATED,order.id:1001
     * @param pageable 分页参数，默认每页20条，按id降序
     * @return 分页的结算记录 DTO
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<CheckoutDetailDto>> listCheckoutDetails(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user) {

        Specification<CheckoutDetail> spec = buildSpecification(search);
        Page<CheckoutDetail> page = checkoutDetailRepository.findAll(spec, pageable);
        Page<CheckoutDetailDto> dtoPage = page.map(this::convertToDto);
        return ResponseEntity.ok(dtoPage);
    }

    // ---------- 辅助方法 ----------

    private CheckoutDetailDto convertToDto(CheckoutDetail entity) {
        CheckoutDetailDto dto = new CheckoutDetailDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrder() != null ? entity.getOrder().getId() : null);
        dto.setWithdrawId(entity.getWithdraw() != null ? entity.getWithdraw().getId() : null);
        dto.setRole(entity.getRole() != null ? entity.getRole().name() : null);
        dto.setUsername(entity.getUsername());
        dto.setRatio(entity.getRatio());
        dto.setNetProfit(entity.getNetProfit());
        dto.setActualProfit(entity.getActualProfit());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setCreateTimeMs(entity.getCreateTimeMs());
        dto.setConfirmTimeMs(entity.getConfirmTimeMs());
        dto.setReleaseTimeMs(entity.getReleaseTimeMs());
        dto.setFinishTimeMs(entity.getFinishTimeMs());
        dto.setRefundTimeMs(entity.getRefundTimeMs());
        return dto;
    }

    /**
     * 构建动态查询 Specification
     */
    private Specification<CheckoutDetail> buildSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return Specification.where(null);
        }

        List<SearchCriteria> criteriaList = parseSearch(search);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                Path<?> path = resolvePath(root, criteria.getField());
                if (path == null) continue;

                Object value = convertValue(path.getJavaType(), criteria.getValue());

                switch (criteria.getOperator()) {
                    case EQ:
                        predicates.add(cb.equal(path, value));
                        break;
                    case LIKE:
                        if (path.getJavaType() == String.class) {
                            predicates.add(cb.like((Path<String>) path, "%" + value + "%"));
                        }
                        break;
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 解析字段路径（支持点号嵌套，如 order.id）
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
     * 解析 search 字符串为条件列表
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
     * 将字符串值转换为字段对应的 Java 类型（支持枚举、Long、Integer、Double、Boolean）
     */
    private Object convertValue(Class<?> targetType, String value) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType.isEnum()) {
            // 枚举类型按名称转换（假设存储为字符串）
            return Enum.valueOf((Class<Enum>) targetType, value);
        } else {
            return value; // 其他类型按字符串处理（可能出错，但可扩展）
        }
    }

    // ---------- 内部类 ----------

    @Getter
    @Setter
    public static class CheckoutDetailDto {
        private Long id;
        private Long orderId;
        private Long withdrawId;
        private String role;          // 枚举名称
        private String username;
        private BigDecimal ratio;
        private BigDecimal netProfit;
        private BigDecimal actualProfit;
        private String status;         // 状态枚举名称
        private Long createTimeMs;
        private Long confirmTimeMs;
        private Long releaseTimeMs;
        private Long finishTimeMs;
        private Long refundTimeMs;
    }

    /**
     * 查询条件内部类
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
        LIKE     // 模糊查询（仅字符串）
    }
}
