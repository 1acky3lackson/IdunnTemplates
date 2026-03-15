package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutWithdrawAllocation;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseWithdraw;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutWithdrawAllocationRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseWithdrawService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/commercial/netease-withdraws")
@AllArgsConstructor
public class NeteaseWithdrawController {

    private final NeteaseWithdrawService withdrawService;
    private final CheckoutWithdrawAllocationRepository allocationRepository;

    /**
     * 分页获取提现记录，支持动态搜索
     * 格式示例：username:admin,ratio~:0.8
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<NeteaseWithdraw>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "saveTimeMs", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user
    ) {
        // 权限验证部分已留空

        Specification<NeteaseWithdraw> spec = buildSpecification(search);
        return ResponseEntity.ok(withdrawService.findAll(spec, pageable));
    }

    /**
     * 新增提现记录
     */
    @PostMapping
    @AuthRequired
    public ResponseEntity<NeteaseWithdraw> create(@RequestBody NeteaseWithdraw withdraw, UserContext user) {
        // 权限验证部分已留空

        withdraw.setUsername(user.getUsername());
        NeteaseWithdraw saved = withdrawService.create(withdraw);
        return ResponseEntity.ok(saved);
    }

    // ---------- 以下为复用自 NeteaseOrderController 的动态解析逻辑 ----------

    private Specification<NeteaseWithdraw> buildSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return Specification.where(null);
        }

        List<SearchCriteria> criteriaList = parseSearch(search);

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                try {
                    Path<?> path = resolvePath(root, criteria.getField());
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
                } catch (Exception e) {
                    // 忽略无法解析的路径或转换失败的条件
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
        if (targetType == String.class) return value;
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == BigDecimal.class) return new BigDecimal(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        if (targetType.isEnum()) return Enum.valueOf((Class<Enum>) targetType, value);
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

    private enum Operator { EQ, LIKE }

    /**
     * 查询指定提现记录的使用分配详情（即这笔钱花在了哪些地方）
     * * @param withdrawId 提现记录ID
     * @param search     动态筛选条件，例如：checkoutDetail.id:123
     * @param pageable   分页参数，默认按创建时间倒序
     */
    @GetMapping("/{withdrawId}/allocations")
    @AuthRequired
    public ResponseEntity<Page<CheckoutWithdrawAllocation>> listAllocations(
            @PathVariable Long withdrawId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createTimeMs", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user
    ) {
        // 构建基础 Specification：限定 withdraw.id
        Specification<CheckoutWithdrawAllocation> baseSpec = (root, query, cb) ->
                cb.equal(root.get("withdraw").get("id"), withdrawId);

        // 解析 search 字符串并合并
        Specification<CheckoutWithdrawAllocation> dynamicSpec = buildAllocationSpecification(search);

        Page<CheckoutWithdrawAllocation> result = allocationRepository.findAll(
                baseSpec.and(dynamicSpec),
                pageable
        );

        return ResponseEntity.ok(result);
    }

    /**
     * 专门为 Allocation 实体构建的动态查询解析器
     */
    private Specification<CheckoutWithdrawAllocation> buildAllocationSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return Specification.where(null);
        }

        List<SearchCriteria> criteriaList = parseSearch(search);

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                try {
                    // resolvePath 支持嵌套，如 "checkoutDetail.status"
                    Path<?> path = resolvePath(root, criteria.getField());
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
                } catch (Exception ignored) {}
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}