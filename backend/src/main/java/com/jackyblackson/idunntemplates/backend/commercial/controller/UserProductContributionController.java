package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.dto.checkout.ContributionDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.CommercialRoleType;
import com.jackyblackson.idunntemplates.backend.commercial.entity.checkout.UserProjectContribution;
import com.jackyblackson.idunntemplates.backend.commercial.service.UserProjectContributionService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/commercial/products/{productId}/contributions")
@AllArgsConstructor
public class UserProductContributionController {

    private final UserProjectContributionService contributionService;
    private final LuckyPermAuthService luckyPermAuthService;

    @GetMapping
    @AuthRequired
    public ResponseEntity<List<UserProjectContribution>> getContributions(
            @PathVariable Long productId,
            UserContext userContext
    ) {
        return ResponseEntity.ok(contributionService.getAllActiveProductContributions(productId));
    }

    @PostMapping
    @AuthRequired
    public ResponseEntity<Void> addContribution(
            @PathVariable Long productId,
            @Valid @RequestBody ContributionDto.AddRequest request,
            UserContext userContext
    ) {
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.Commercial.Project.Contribution.add)) {
            return ResponseEntity.status(406).build();
        }
        contributionService.addProductContributionAndRecalculate(productId, request, userContext.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recalculate")
    @AuthRequired
    public ResponseEntity<Map<CommercialRoleType, ContributionDto.RecalculatePreviewResponse>> recalculation(
            @PathVariable Long productId,
            UserContext userContext
    ) {
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.Commercial.Project.Contribution.recalculate)) {
            return ResponseEntity.status(406).build();
        }
        return ResponseEntity.ok(contributionService.recalculateProduct(productId));
    }

    @DeleteMapping("/{contributionId}")
    @AuthRequired
    public ResponseEntity<Void> deleteContribution(
            @PathVariable Long productId,
            @PathVariable Long contributionId,
            @Valid @RequestBody ContributionDto.DeleteRequest request,
            UserContext userContext
    ) {
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.Commercial.Project.Contribution.delete)) {
            return ResponseEntity.status(406).build();
        }
        contributionService.softDeleteContribution(contributionId, request.getDeleteReason(), userContext.getUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{contributionId}")
    @AuthRequired
    public ResponseEntity<Void> updateContributionPoints(
            @PathVariable Long productId,
            @PathVariable Long contributionId,
            @Valid @RequestBody ContributionDto.UpdateRequest request,
            UserContext userContext
    ) {
        if (!luckyPermAuthService.checkPermission(userContext, PermissionNames.Commercial.Project.Contribution.modify)) {
            return ResponseEntity.status(406).build();
        }
        contributionService.updateContributionPointsAndRecalculate(
                contributionId,
                request.getContributePoints(),
                userContext.getUsername()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/grouped")
    @AuthRequired
    public ResponseEntity<Map<CommercialRoleType, List<UserProjectContribution>>> getContributionsGroupedByRole(
            @PathVariable Long productId,
            UserContext userContext
    ) {
        return ResponseEntity.ok(contributionService.getProductContributionsGroupedByRole(productId));
    }
}
