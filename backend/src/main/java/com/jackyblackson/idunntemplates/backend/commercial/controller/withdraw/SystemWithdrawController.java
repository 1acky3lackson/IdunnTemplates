package com.jackyblackson.idunntemplates.backend.commercial.controller.withdraw;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw.SystemWithdrawCreateRequest;
import com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw.SystemWithdrawDto;
import com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw.SystemWithdrawStatusUpdateRequest;
import com.jackyblackson.idunntemplates.backend.commercial.service.withdraw.SystemWithdrawService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commercial/withdrawals")
@RequiredArgsConstructor
public class SystemWithdrawController {

    private final SystemWithdrawService systemWithdrawService;
    private final LuckyPermAuthService authService;

    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<SystemWithdrawDto>> listAllWithdrawals(
            @PageableDefault(size = 20, sort = "createTimeMs", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user
    ) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.SystemWithdraw.listAll)) {
            return ResponseEntity.status(406).build();
        }
        return ResponseEntity.ok(systemWithdrawService.getAllWithdrawals(pageable));
    }

    @GetMapping("/me")
    @AuthRequired
    public ResponseEntity<Page<SystemWithdrawDto>> listMyWithdrawals(
            @PageableDefault(size = 20, sort = "createTimeMs", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user
    ) {
        return ResponseEntity.ok(systemWithdrawService.getWithdrawalsByUser(user.getUsername(), pageable));
    }

    @GetMapping("/{id}")
    @AuthRequired
    public ResponseEntity<SystemWithdrawDto> getWithdrawal(
            @PathVariable Long id,
            UserContext user
    ) {
        SystemWithdrawDto withdraw = systemWithdrawService.getWithdrawalById(id);

        // 只有所有者或者具有管理权限的人可以查看
        if (!withdraw.getUsername().equals(user.getUsername()) &&
                !authService.checkPermission(user, PermissionNames.Commercial.SystemWithdraw.listAll)) {
            return ResponseEntity.status(406).build();
        }

        return ResponseEntity.ok(withdraw);
    }

    @PostMapping
    @AuthRequired
    public ResponseEntity<SystemWithdrawDto> createWithdrawal(
            @RequestBody SystemWithdrawCreateRequest request,
            UserContext user
    ) {
        return ResponseEntity.ok(systemWithdrawService.createWithdrawal(user.getUsername(), request));
    }

    @PatchMapping("/{id}/status")
    @AuthRequired
    public ResponseEntity<SystemWithdrawDto> updateWithdrawalStatus(
            @PathVariable Long id,
            @RequestBody SystemWithdrawStatusUpdateRequest request,
            UserContext user
    ) {
        boolean isAdmin = authService.checkPermission(user, PermissionNames.Commercial.SystemWithdraw.manage);
        return ResponseEntity.ok(systemWithdrawService.updateWithdrawalStatus(id, request, user.getUsername(), isAdmin));
    }
}
