package com.jackyblackson.idunntemplates.backend.commercial.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSettlementTriggerService {

    private final CheckoutCalculationService checkoutCalculationService;
    private final CheckoutDetailService checkoutDetailService;

    public void runSettlementPipeline() {
        log.info("开始执行即时订单结算链路");
        checkoutCalculationService.processAllEnteredOrders();
        checkoutCalculationService.processAllCreatedDetails();
        checkoutDetailService.releaseConfirmedDetails();
    }
}
