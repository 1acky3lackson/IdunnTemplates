package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.GlobalContextUpdateRequest;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.GlobalCheckoutParamContext;
import com.jackyblackson.idunntemplates.backend.commercial.service.GlobalCheckoutParamContextService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/commercial/global-contexts")
@AllArgsConstructor
public class GlobalCheckoutParamContextController {


    private final GlobalCheckoutParamContextService service;
    private final LuckyPermAuthService luckyPermAuthService;

    /**
     * 获取当前有效的全局参数配置。
     * 如果不存在有效记录，会自动创建一条默认配置。
     */
    @GetMapping("/current")
    @AuthRequired
    public ResponseEntity<GlobalCheckoutParamContext> getCurrent() {
        GlobalCheckoutParamContext current = service.getEffectiveConfig();
        return ResponseEntity.ok(current);
    }

    /**
     * 获取所有历史配置记录（包含已禁用的），按创建时间倒序排列。
     */
    @GetMapping()
    @AuthRequired
    public ResponseEntity<List<GlobalCheckoutParamContext>> getHistory(UserContext user) {
        if (!luckyPermAuthService.checkPermission(user, PermissionNames.Commercial.CheckoutParam.list)) {
            return ResponseEntity.status(406).build();
        }
        List<GlobalCheckoutParamContext> history = service.getAllConfigs();
        return ResponseEntity.ok(history);
    }

    /**
     * 更新全局参数配置。
     * 将当前有效记录标记为禁用，并根据请求参数创建一条新记录。
     *
     * @param request 包含新参数及更新原因、操作人信息
     * @return 新创建的参数配置
     */
    @PostMapping
    @AuthRequired
    public ResponseEntity<GlobalCheckoutParamContext> updateConfig(
            @Valid @RequestBody GlobalContextUpdateRequest request,
            UserContext userContext
    ) {
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.Commercial.CheckoutParam.update)) {
            return ResponseEntity.status(406).build();
        }
        // 将请求参数转换为实体（只传递数值字段，ID 和创建时间等由 service 处理）
        GlobalCheckoutParamContext newConfig = new GlobalCheckoutParamContext();
        newConfig.setTaixueRatio(request.getTaixueRatio());
        newConfig.setCommercialRatio(request.getCommercialRatio());
        newConfig.setTemplateDefectParam(request.getTemplateDefectParam());
        newConfig.setPlacerRatio(request.getPlacerRatio());
        newConfig.setUploaderRatio(request.getUploaderRatio());
        newConfig.setReleaseDelayDays(request.getReleaseDelayDays());

        GlobalCheckoutParamContext updated = service.updateConfig(
                newConfig,
                request.getUpdateReason(),
                userContext.getUsername()
        );
        return ResponseEntity.ok(updated);
    }
}
