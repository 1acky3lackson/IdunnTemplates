package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.service.CheckoutCalculationService;
import com.jackyblackson.idunntemplates.backend.commercial.service.CheckoutDetailService;
import com.jackyblackson.idunntemplates.backend.commercial.service.CrawlerSyncService;
import com.jackyblackson.idunntemplates.backend.commercial.service.NeteaseOrderSyncService;
import com.jackyblackson.idunntemplates.backend.commercial.service.OrderSettlementTriggerService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commercial/admin")
@AllArgsConstructor
public class CommercialAdminController {
    private final CheckoutCalculationService checkoutCalculationService;
    private final LuckyPermAuthService authService;
    private final CheckoutDetailService checkoutDetailService;
    private final CrawlerSyncService crawlerSyncService;
    private final NeteaseOrderSyncService neteaseOrderSyncService;
    private final OrderSettlementTriggerService orderSettlementTriggerService;

    @GetMapping()
    @AuthRequired
    public ResponseEntity<Void> checkAdminPermission(UserContext user) {
        if (authService.checkPermission(user, PermissionNames.Commercial.Admin.admin)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(406).build();
    }

    @GetMapping("/calculate/order")
    @AuthRequired
    public ResponseEntity<Void> checkoutOrder(UserContext user) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Admin.triggerCheckoutOrder)) {
            return ResponseEntity.status(406).build();
        }
        orderSettlementTriggerService.runSettlementPipeline();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calculate/checkout-detail")
    @AuthRequired
    public ResponseEntity<Void> calculateCheckoutDetail(UserContext user) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Admin.triggerCheckoutDetail)) {
            return ResponseEntity.status(406).build();
        }
        checkoutCalculationService.processAllCreatedDetails();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calculate/release")
    @AuthRequired
    public ResponseEntity<Void> calculateCheckoutDetailRelease(UserContext user) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Admin.triggerReleaseBalance)) {
            return ResponseEntity.status(406).build();
        }
        checkoutDetailService.releaseConfirmedDetails();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sync/ne-product")
    @AuthRequired
    public ResponseEntity<Void> syncCrawlerNeProductLog(UserContext user) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Admin.syncNeProduct)) {
            return ResponseEntity.status(406).build();
        }
        crawlerSyncService.syncAllWithPaging();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sync/ne-order")
    @AuthRequired
    public ResponseEntity<Void> syncCrawlerNeProductOrderLog(UserContext user) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Admin.syncNeOrder)) {
            return ResponseEntity.status(406).build();
        }
        neteaseOrderSyncService.syncOrdersFromLogs();
        return ResponseEntity.ok().build();
    }
}
