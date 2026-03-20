package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.dto.CreateServerDto;
import com.jackyblackson.idunntemplates.backend.dto.TrustedServerDto;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.TrustedServerService;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servers")
public class TrustedServerController {

    private final TrustedServerService trustedServerService;
    private final LuckyPermAuthService luckyPermAuthService;

    public TrustedServerController(TrustedServerService trustedServerService, LuckyPermAuthService luckyPermAuthService) {
        this.trustedServerService = trustedServerService;
        this.luckyPermAuthService = luckyPermAuthService;
    }

    @PostMapping
    @AuthRequired
    public ResponseEntity<TrustedServerDto> createServer(@RequestBody CreateServerDto dto, UserContext userContext) {
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.TrustedServers.create)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        TrustedServerDto created = trustedServerService.createServer(dto, userContext.getUsername());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @AuthRequired
    public ResponseEntity<List<TrustedServerDto>> getServers(UserContext userContext) {
        // 权限校验
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.TrustedServers.list)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        return ResponseEntity.ok(trustedServerService.getAllServers());
    }

    @DeleteMapping("/{id}")
    @AuthRequired
    public ResponseEntity<Void> deleteServer(@PathVariable Long id, UserContext userContext) {
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.TrustedServers.delete)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        trustedServerService.deleteServer(id);
        return ResponseEntity.ok().build();
    }
}
