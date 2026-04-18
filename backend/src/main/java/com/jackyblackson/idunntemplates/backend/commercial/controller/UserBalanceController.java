package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.dto.balance.BalanceInfo;
import com.jackyblackson.idunntemplates.backend.commercial.dto.balance.UserBalanceListItemDto;
import com.jackyblackson.idunntemplates.backend.commercial.dto.balance.UserBalanceRecordDto;
import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.SettlementBreakdownDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalance;
import com.jackyblackson.idunntemplates.backend.commercial.entity.balance.UserBalanceRecord;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CheckoutDetail;
import com.jackyblackson.idunntemplates.backend.commercial.repository.balance.UserBalanceRecordRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.balance.UserBalanceRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.CheckoutDetailRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.BalanceService;
import com.jackyblackson.idunntemplates.backend.commercial.service.CheckoutDetailService;
import com.jackyblackson.idunntemplates.backend.commercial.service.OrderSettlementBreakdownService;
import com.jackyblackson.idunntemplates.backend.commercial.service.UserBalanceService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/commercial/balance")
@RequiredArgsConstructor
public class UserBalanceController {

    private final BalanceService balanceService;
    private final UserBalanceRepository userBalanceRepository;
    private final CheckoutDetailRepository checkoutDetailRepository;
    private final UserBalanceRecordRepository userBalanceRecordRepository;
    private final UserBalanceService userBalanceService;
    private final CheckoutDetailService checkoutDetailService;
    private final OrderSettlementBreakdownService orderSettlementBreakdownService;
    private final LuckyPermAuthService luckyPermAuthService;

    // 辅助方法：将 CheckoutDetail 转换为 DTO
    private CheckoutDetailController.CheckoutDetailDto toCheckoutDetailDto(CheckoutDetail detail) {
        CheckoutDetailController.CheckoutDetailDto dto = new CheckoutDetailController.CheckoutDetailDto();
        dto.setId(detail.getId());
        dto.setStatus(String.valueOf(detail.getStatus()));
        if (detail.getOrder() != null) {
            dto.setOrderId(detail.getOrder().getId());
            // 如果需要订单信息，可以添加更多字段，比如订单号
        }
        dto.setRole(String.valueOf(detail.getRole()));
        dto.setUsername(detail.getUsername());
        dto.setRatio(detail.getRatio());
        dto.setNetProfit(detail.getNetProfit());
        dto.setActualProfit(detail.getActualProfit());
        dto.setCreateTimeMs(detail.getCreateTimeMs());
        dto.setConfirmTimeMs(detail.getConfirmTimeMs());
        dto.setReleaseTimeMs(detail.getReleaseTimeMs());
        dto.setFinishTimeMs(detail.getFinishTimeMs());
        dto.setRefundTimeMs(detail.getRefundTimeMs());
        return dto;
    }

    // 辅助方法：将 UserBalanceRecord 转换为 DTO
    private UserBalanceRecordDto toUserBalanceRecordDto(UserBalanceRecord record) {
        UserBalanceRecordDto dto = new UserBalanceRecordDto();
        dto.setId(record.getId());
        dto.setUsername(record.getUsername());
        dto.setType(record.getType());
        dto.setAmount(record.getAmount());
        dto.setBalanceBefore(record.getBalanceBefore());
        dto.setBalanceAfter(record.getBalanceAfter());
        dto.setRelatedId(record.getRelatedId());
        dto.setDescription(record.getDescription());
        dto.setCreateTimeMs(record.getCreateTimeMs());
        return dto;
    }

    // ---------- 1. 查看自己的账户余额 ----------
    @GetMapping()
    @AuthRequired
    public ResponseEntity<BalanceInfo> getMyBalance(
            UserContext user) {
        // 通常用户名可以从认证信息中获取，这里作为示例传入参数
        BalanceInfo info = balanceService.getInfo(user.getUsername());
        return ResponseEntity.ok(info);
    }

    // ---------- 2. 查看系统用户的余额列表（分页） ----------
    @GetMapping("/users")
    @AuthRequired
    public ResponseEntity<Page<BalanceInfo>> listUserBalances(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable,
            UserContext user) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.Commercial.Balance.list)) {
            return ResponseEntity.status(406).build();
        }
        // 构建查询条件，筛选 UserBalance 表
        Specification<UserBalance> spec = buildUserBalanceSpecification(search);
        Page<UserBalance> page = userBalanceRepository.findAll(spec, pageable);

        // 转换为 DTO，只包含用户名和可用余额
        Page<BalanceInfo> dtoPage = page.map(ub -> balanceService.getInfo(ub.getUsername()));
        return ResponseEntity.ok(dtoPage);
    }

    // ---------- 3. 查看结账单列表（分页、筛选、排序） ----------
    @GetMapping("/checkout-details")
    @AuthRequired
    public ResponseEntity<Page<CheckoutDetailController.CheckoutDetailDto>> listCheckoutDetails(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user) {

        boolean checkoutAll = luckyPermAuthService.checkPermission(user,
                PermissionNames.Commercial.Balance.checkoutAll);

        Specification<CheckoutDetail> spec = buildCheckoutDetailSpecification(search);
        Page<CheckoutDetail> page = checkoutDetailRepository.findAll(spec, pageable);

        Page<CheckoutDetailController.CheckoutDetailDto> dtoPage = page.map(this::toCheckoutDetailDto);

        if (!checkoutAll) {
            dtoPage = dtoPage.map(dto -> dto.getUsername().equals(user.getUsername()) ? dto : null);
        }

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/checkout-details/{id}/settlement-breakdown")
    @AuthRequired
    public ResponseEntity<SettlementBreakdownDto> getCheckoutDetailSettlementBreakdown(
            @PathVariable Long id,
            UserContext user) {
        var detailOptional = checkoutDetailRepository.findById(id);
        if (detailOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean checkoutAll = luckyPermAuthService.checkPermission(user, PermissionNames.Commercial.Balance.checkoutAll);
        if (!checkoutAll && !detailOptional.get().getUsername().equals(user.getUsername())) {
            return ResponseEntity.status(406).build();
        }
        return orderSettlementBreakdownService.buildForCheckoutDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- 4. 查看虚拟点数变动列表（分页、筛选、排序） ----------
    @GetMapping("/records")
    @AuthRequired
    public ResponseEntity<Page<UserBalanceRecordDto>> listBalanceRecords(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createTimeMs", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user) {
        boolean checkoutAll = luckyPermAuthService.checkPermission(user,
                PermissionNames.Commercial.Balance.transactionAll);

        Specification<UserBalanceRecord> spec = buildBalanceRecordSpecification(search);
        Page<UserBalanceRecord> page = userBalanceRecordRepository.findAll(spec, pageable);

        Page<UserBalanceRecordDto> dtoPage = page.map(this::toUserBalanceRecordDto);

        if (!checkoutAll) {
            dtoPage = dtoPage.map(dto -> dto.getUsername().equals(user.getUsername()) ? dto : null);
        }

        return ResponseEntity.ok(dtoPage);
    }

    // ---------- 构建 Specification 的辅助方法 ----------

    private Specification<UserBalance> buildUserBalanceSpecification(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.trim().isEmpty()) {
                // 简单支持 username 的等值或模糊查询
                // 格式：username:xxx 或 username~:xxx
                String[] parts = search.split(":", 2);
                if (parts.length == 2) {
                    String fieldOp = parts[0].trim();
                    String value = parts[1].trim();
                    boolean like = fieldOp.endsWith("~");
                    String field = like ? fieldOp.substring(0, fieldOp.length() - 1).trim() : fieldOp;
                    if ("username".equalsIgnoreCase(field)) {
                        if (like) {
                            predicates.add(cb.like(root.get("username"), "%" + value + "%"));
                        } else {
                            predicates.add(cb.equal(root.get("username"), value));
                        }
                    }
                    // 可扩展其他字段
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<CheckoutDetail> buildCheckoutDetailSpecification(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.trim().isEmpty()) {
                String[] conditions = search.split(",");
                for (String condition : conditions) {
                    String[] parts = condition.split(":", 2);
                    if (parts.length != 2)
                        continue;
                    String fieldOp = parts[0].trim();
                    String value = parts[1].trim();
                    boolean like = fieldOp.endsWith("~");
                    String field = like ? fieldOp.substring(0, fieldOp.length() - 1).trim() : fieldOp;
                    Path<?> path = resolvePath(root, field);
                    if (path == null)
                        continue;
                    Object converted = convertValue(path.getJavaType(), value);
                    if (like && path.getJavaType() == String.class) {
                        predicates.add(cb.like((Path<String>) path, "%" + value + "%"));
                    } else {
                        predicates.add(cb.equal(path, converted));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<UserBalanceRecord> buildBalanceRecordSpecification(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.trim().isEmpty()) {
                String[] conditions = search.split(",");
                for (String condition : conditions) {
                    String[] parts = condition.split(":", 2);
                    if (parts.length != 2)
                        continue;
                    String fieldOp = parts[0].trim();
                    String value = parts[1].trim();
                    boolean like = fieldOp.endsWith("~");
                    String field = like ? fieldOp.substring(0, fieldOp.length() - 1).trim() : fieldOp;
                    Path<?> path = resolvePath(root, field);
                    if (path == null)
                        continue;
                    Object converted = convertValue(path.getJavaType(), value);
                    if (like && path.getJavaType() == String.class) {
                        predicates.add(cb.like((Path<String>) path, "%" + value + "%"));
                    } else {
                        predicates.add(cb.equal(path, converted));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
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
            return Enum.valueOf((Class<Enum>) targetType, value);
        } else {
            return value;
        }
    }
}
